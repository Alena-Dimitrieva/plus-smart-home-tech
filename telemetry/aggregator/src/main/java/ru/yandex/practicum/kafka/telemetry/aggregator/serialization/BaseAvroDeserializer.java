package ru.yandex.practicum.kafka.telemetry.aggregator.serialization;

import org.apache.avro.Schema;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public abstract class BaseAvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {

    private final Schema schema;
    private final DecoderFactory decoderFactory = DecoderFactory.get();
    private final SpecificDatumReader<T> reader;

    public BaseAvroDeserializer(Schema schema) {
        this.schema = schema;
        this.reader = new SpecificDatumReader<>(schema);
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        try (ByteArrayInputStream in = new ByteArrayInputStream(data)) {
            Decoder decoder = decoderFactory.binaryDecoder(in, null);
            return reader.read(null, decoder);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка десериализации Avro из топика " + topic, e);
        }
    }

    @Override
    public void close() {
    }
}