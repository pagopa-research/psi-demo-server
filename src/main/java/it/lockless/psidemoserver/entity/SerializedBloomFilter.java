package it.lockless.psidemoserver.entity;

import javax.persistence.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

@Entity
@Table(name = "serialized_bloom_filter")
public class SerializedBloomFilter {

    @Id
    @GeneratedValue(generator = "serialized_bloom_filter_id_seq", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name="serialized_bloom_filter_id_seq",sequenceName="serialized_bloom_filter_id_seq")
    @Column(name = "id")
    private long id;

    @Column(name = "serialized_value")
    private byte[] serializedValue;

    @Column(name = "bloom_filter_creation_date")
    private Instant bloomFilterCreationDate;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public byte[] getSerializedValue() {
        return serializedValue;
    }

    public void setSerializedValue(byte[] serializedBloomFilter) {
        this.serializedValue = serializedBloomFilter;
    }

    public Instant getBloomFilterCreationDate() {
        return bloomFilterCreationDate;
    }

    public void setBloomFilterCreationDate(Instant bloomFilterCreationDate) {
        this.bloomFilterCreationDate = bloomFilterCreationDate;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SerializedBloomFilter that = (SerializedBloomFilter) o;
        return id == that.id && Arrays.equals(serializedValue, that.serializedValue) && Objects.equals(bloomFilterCreationDate, that.bloomFilterCreationDate);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, bloomFilterCreationDate);
        result = 31 * result + Arrays.hashCode(serializedValue);
        return result;
    }

    @Override
    public String toString() {
        return "SerializedBloomFilter{" +
                "id=" + id +
                ", serializedBloomFilter=" + Arrays.toString(serializedValue) +
                ", bloomFilterCreationDate=" + bloomFilterCreationDate +
                '}';
    }
}