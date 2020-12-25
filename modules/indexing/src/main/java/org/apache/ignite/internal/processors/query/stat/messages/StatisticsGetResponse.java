package org.apache.ignite.internal.processors.query.stat.messages;

import org.apache.ignite.internal.GridDirectCollection;
import org.apache.ignite.plugin.extensions.communication.Message;
import org.apache.ignite.plugin.extensions.communication.MessageReader;
import org.apache.ignite.plugin.extensions.communication.MessageWriter;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

/**
 * Response with currently existing statistics.
 */
public class StatisticsGetResponse implements Message {
    /** */
    private static final long serialVersionUID = 0L;

    /** */
    public static final short TYPE_CODE = 188;

    /** Request id. */
    private UUID reqId;

    /** List of keys to supply statistics by. */
    @GridDirectCollection(StatisticsObjectData.class)
    private List<StatisticsObjectData> data;

    /**
     * Default constructor.
     */
    public StatisticsGetResponse() {
    }

    /**
     * Constructor.
     *
     * @param reqId Request id.
     * @param data Statistics data.
     */
    public StatisticsGetResponse(UUID reqId, List<StatisticsObjectData> data) {
        this.reqId = reqId;
        this.data = data;
    }

    /**
     * @return Request id.
     */
    public UUID reqId() {
        return reqId;
    }

    /**
     * @return Statistics.
     */
    public List<StatisticsObjectData> data() {
        return data;
    }

    /** {@inheritDoc} */
    @Override public boolean writeTo(ByteBuffer buf, MessageWriter writer) {
        return false;
    }

    /** {@inheritDoc} */
    @Override public boolean readFrom(ByteBuffer buf, MessageReader reader) {
        return false;
    }

    /** {@inheritDoc} */
    @Override public short directType() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override public byte fieldsCount() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override public void onAckReceived() {

    }
}
