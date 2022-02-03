package it.lockless.psidemoserver.model;

import it.lockless.psidemoserver.entity.SerializedBloomFilter;

import java.time.Instant;
import java.util.Arrays;

public class BloomFilterDTO {

    private byte[] serializedBloomFilter;
    private Instant bloomFilterCreationDate;

    public BloomFilterDTO() {
    }

    public BloomFilterDTO(SerializedBloomFilter serializedBloomFilter){
        this.serializedBloomFilter = serializedBloomFilter.getSerializedValue();
        this.bloomFilterCreationDate = serializedBloomFilter.getBloomFilterCreationDate();
    }

    public byte[] getSerializedBloomFilter() {
        return serializedBloomFilter;
    }

    public void setSerializedBloomFilter(byte[] serializedBloomFilter) {
        this.serializedBloomFilter = serializedBloomFilter;
    }

    public Instant getBloomFilterCreationDate() {
        return bloomFilterCreationDate;
    }

    public void setBloomFilterCreationDate(Instant bloomFilterCreationDate) {
        this.bloomFilterCreationDate = bloomFilterCreationDate;
    }

    @Override
    public String toString() {
        return "BloomFilterDTO{" +
                "serializedBloomFilter=" + Arrays.toString(serializedBloomFilter) +
                ", bloomFilterCreationDate=" + bloomFilterCreationDate +
                '}';
    }
}
