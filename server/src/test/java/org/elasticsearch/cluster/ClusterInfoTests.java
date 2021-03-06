/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */
package org.elasticsearch.cluster;

import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.cluster.routing.ShardRoutingState;
import org.elasticsearch.cluster.routing.TestShardRouting;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.index.shard.ShardId;
import org.elasticsearch.test.ESTestCase;

import java.util.HashMap;
import java.util.Map;

public class ClusterInfoTests extends ESTestCase {

    public void testSerialization() throws Exception {
        ClusterInfo clusterInfo = new ClusterInfo(
            randomDiskUsage(),
            randomDiskUsage(),
            randomShardSizes(),
            randomDataSetSizes(),
            randomRoutingToDataPath(),
            randomReservedSpace()
        );
        BytesStreamOutput output = new BytesStreamOutput();
        clusterInfo.writeTo(output);

        ClusterInfo result = new ClusterInfo(output.bytes().streamInput());
        assertEquals(clusterInfo.getNodeLeastAvailableDiskUsages(), result.getNodeLeastAvailableDiskUsages());
        assertEquals(clusterInfo.getNodeMostAvailableDiskUsages(), result.getNodeMostAvailableDiskUsages());
        assertEquals(clusterInfo.shardSizes, result.shardSizes);
        assertEquals(clusterInfo.shardDataSetSizes, result.shardDataSetSizes);
        assertEquals(clusterInfo.routingToDataPath, result.routingToDataPath);
        assertEquals(clusterInfo.reservedSpace, result.reservedSpace);
    }

    private static Map<String, DiskUsage> randomDiskUsage() {
        int numEntries = randomIntBetween(0, 128);
        Map<String, DiskUsage> builder = new HashMap<>(numEntries);
        for (int i = 0; i < numEntries; i++) {
            String key = randomAlphaOfLength(32);
            DiskUsage diskUsage = new DiskUsage(
                randomAlphaOfLength(4),
                randomAlphaOfLength(4),
                randomAlphaOfLength(4),
                randomIntBetween(0, Integer.MAX_VALUE),
                randomIntBetween(0, Integer.MAX_VALUE)
            );
            builder.put(key, diskUsage);
        }
        return builder;
    }

    private static Map<String, Long> randomShardSizes() {
        int numEntries = randomIntBetween(0, 128);
        Map<String, Long> builder = new HashMap<>(numEntries);
        for (int i = 0; i < numEntries; i++) {
            String key = randomAlphaOfLength(32);
            long shardSize = randomIntBetween(0, Integer.MAX_VALUE);
            builder.put(key, shardSize);
        }
        return builder;
    }

    private static Map<ShardId, Long> randomDataSetSizes() {
        int numEntries = randomIntBetween(0, 128);
        Map<ShardId, Long> builder = new HashMap<>(numEntries);
        for (int i = 0; i < numEntries; i++) {
            ShardId key = new ShardId(randomAlphaOfLength(10), randomAlphaOfLength(10), between(0, Integer.MAX_VALUE));
            long shardSize = randomIntBetween(0, Integer.MAX_VALUE);
            builder.put(key, shardSize);
        }
        return builder;
    }

    private static Map<ShardRouting, String> randomRoutingToDataPath() {
        int numEntries = randomIntBetween(0, 128);
        Map<ShardRouting, String> builder = new HashMap<>(numEntries);
        for (int i = 0; i < numEntries; i++) {
            ShardId shardId = new ShardId(randomAlphaOfLength(32), randomAlphaOfLength(32), randomIntBetween(0, Integer.MAX_VALUE));
            ShardRouting shardRouting = TestShardRouting.newShardRouting(shardId, null, randomBoolean(), ShardRoutingState.UNASSIGNED);
            builder.put(shardRouting, randomAlphaOfLength(32));
        }
        return builder;
    }

    private static Map<ClusterInfo.NodeAndPath, ClusterInfo.ReservedSpace> randomReservedSpace() {
        int numEntries = randomIntBetween(0, 128);
        Map<ClusterInfo.NodeAndPath, ClusterInfo.ReservedSpace> builder = new HashMap<>(numEntries);
        for (int i = 0; i < numEntries; i++) {
            final ClusterInfo.NodeAndPath key = new ClusterInfo.NodeAndPath(randomAlphaOfLength(10), randomAlphaOfLength(10));
            final ClusterInfo.ReservedSpace.Builder valueBuilder = new ClusterInfo.ReservedSpace.Builder();
            for (int j = between(0, 10); j > 0; j--) {
                ShardId shardId = new ShardId(randomAlphaOfLength(32), randomAlphaOfLength(32), randomIntBetween(0, Integer.MAX_VALUE));
                valueBuilder.add(shardId, between(0, Integer.MAX_VALUE));
            }
            builder.put(key, valueBuilder.build());
        }
        return builder;
    }

}
