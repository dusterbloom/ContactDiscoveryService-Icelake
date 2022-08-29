/*
 * Copyright 2022 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.signal.cdsi.redis;

import static org.junit.jupiter.api.Assumptions.assumeFalse;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.SlotHash;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import redis.embedded.RedisServer;

public class RedisClusterExtension implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback, AfterEachCallback {
  private static final int CLUSTER_STARTUP_TIMEOUT_MILLIS = 30000;

  public static RedisClusterExtensionBuilder builder() {
    return new RedisClusterExtensionBuilder();
  }


  @Override
  public void afterAll(final ExtensionContext context) {
    for (final RedisServer node : clusterNodes) {
      node.stop();
    }
  }

  @Override
  public void afterEach(final ExtensionContext context) {
    redisClusterClient.shutdown();
  }

  @Override
  public void beforeAll(final ExtensionContext context) throws Exception {
    assumeFalse(System.getProperty("os.name").equalsIgnoreCase("windows"));

    clusterNodes = new RedisServer[NODE_COUNT];

    for (int i = 0; i < NODE_COUNT; i++) {
      clusterNodes[i] = buildClusterNode(getNextRedisClusterPort());
      clusterNodes[i].start();
    }

    assembleCluster(clusterNodes);
  }

  @Override
  public void beforeEach(final ExtensionContext context) {
    final List<String> urls = Arrays.stream(clusterNodes)
        .map(node -> String.format("redis://127.0.0.1:%d", node.ports().get(0)))
        .toList();

    redisClusterClient = RedisClusterClient.create(urls.stream().map(RedisURI::create).collect(Collectors.toList()));

    try (final StatefulRedisClusterConnection<String, String> connection = redisClusterClient.connect()) {
      connection.sync().flushall();
    }
  }

  private static final int NODE_COUNT = 2;

  private static RedisServer[] clusterNodes;

  private RedisClusterClient redisClusterClient;

  public RedisClusterClient getRedisClusterClient() {
    return redisClusterClient;
  }

  private static RedisServer buildClusterNode(final int port) throws IOException {
    final File clusterConfigFile = File.createTempFile("redis", ".conf");
    clusterConfigFile.deleteOnExit();

    return RedisServer.builder()
        .setting("cluster-enabled yes")
        .setting("cluster-config-file " + clusterConfigFile.getAbsolutePath())
        .setting("cluster-node-timeout 5000")
        .setting("appendonly no")
        .setting("save \"\"")
        .setting("dir " + System.getProperty("java.io.tmpdir"))
        .port(port)
        .build();
  }

  private static void assembleCluster(final RedisServer... nodes) throws InterruptedException {
    final RedisClient client = RedisClient.create(RedisURI.create("127.0.0.1", nodes[0].ports().get(0)));

    try (final StatefulRedisConnection<String, String> connection = client.connect()) {
      final RedisCommands<String, String> commands = connection.sync();

      for (int i = 1; i < nodes.length; i++) {
        commands.clusterMeet("127.0.0.1", nodes[i].ports().get(0));
      }
    } finally {
      client.shutdown();
    }

    final int slotsPerNode = SlotHash.SLOT_COUNT / nodes.length;

    for (int i = 0; i < nodes.length; i++) {
      final int startInclusive = i * slotsPerNode;
      final int endExclusive   = i == nodes.length - 1 ? SlotHash.SLOT_COUNT : (i + 1) * slotsPerNode;

      final RedisClient assignSlotClient = RedisClient.create(RedisURI.create("127.0.0.1", nodes[i].ports().get(0)));

      try (final StatefulRedisConnection<String, String> assignSlotConnection = assignSlotClient.connect()) {
        final int[] slots = new int[endExclusive - startInclusive];

        for (int s = startInclusive; s < endExclusive; s++) {
          slots[s - startInclusive] = s;
        }

        assignSlotConnection.sync().clusterAddSlots(slots);
      } finally {
        assignSlotClient.shutdown();
      }
    }

    final RedisClient waitClient = RedisClient.create(RedisURI.create("127.0.0.1", nodes[0].ports().get(0)));

    try (final StatefulRedisConnection<String, String> connection = waitClient.connect()) {
      // CLUSTER INFO gives us a big blob of key-value pairs, but the one we're interested in is `cluster_state`.
      // According to https://redis.io/commands/cluster-info, `cluster_state:ok` means that the node is ready to
      // receive queries, all slots are assigned, and a majority of master nodes are reachable.

      final int sleepMillis = 500;
      int tries = 0;
      while (!connection.sync().clusterInfo().contains("cluster_state:ok")) {
        Thread.sleep(sleepMillis);
        tries++;

        if (tries == CLUSTER_STARTUP_TIMEOUT_MILLIS / sleepMillis) {
          throw new RuntimeException(
              String.format("Timeout: Redis not ready after waiting %d milliseconds", tries * sleepMillis));
        }
      }
    } finally {
      waitClient.shutdown();
    }
  }

  public static int getNextRedisClusterPort() throws IOException {
    final int MAX_ITERATIONS = 11_000;
    int port;
    for (int i = 0; i < MAX_ITERATIONS; i++) {
      try (ServerSocket socket = new ServerSocket(0)) {
        socket.setReuseAddress(false);
        port = socket.getLocalPort();
      }
      if (port < 55535) {
        return port;
      }
    }
    throw new IOException("Couldn't find an open port below 55,535 in " + MAX_ITERATIONS + " tries");
  }

  public static class RedisClusterExtensionBuilder {

    private RedisClusterExtensionBuilder() {

    }

    public RedisClusterExtension build() {
      return new RedisClusterExtension();
    }
  }
}
