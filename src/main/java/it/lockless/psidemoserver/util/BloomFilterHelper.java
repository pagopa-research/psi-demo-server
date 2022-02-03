package it.lockless.psidemoserver.util;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import psi.exception.CustomRuntimeException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public class BloomFilterHelper {

    private static final Logger log = LoggerFactory.getLogger(BloomFilterHelper.class);

    public static BloomFilter<CharSequence> createBloomFilterByteArrayFromSet(Set<String> dataSet, Double falsePositiveProbability){
        log.debug("Called createBloomFilterByteArrayFromStringSet()");
        BloomFilter<CharSequence> bloomFilter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), dataSet.size(), falsePositiveProbability);
        for(String s : dataSet){
            bloomFilter.put(s);
        }
        return bloomFilter;
    }

    public static byte[] convertBloomFilterToByteArray(BloomFilter<CharSequence> bloomFilter){
        log.debug("Called convertBloomFilterByteToByteArray()");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            bloomFilter.writeTo(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new CustomRuntimeException("Error in the Bloom Filter serialization to byte");
        }
    }

    public static BloomFilter<CharSequence> getBloomFilterFromByteArray(byte[] bloomFilterByteArray){
        log.debug("Called getBloomFilterFromByteArray()");
        InputStream inputStream = new ByteArrayInputStream(bloomFilterByteArray);
        try {
            return  BloomFilter.readFrom(inputStream, Funnels.stringFunnel(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new CustomRuntimeException("Cannot deserialize the Bloom Filter");
        }
    }

    public static Set<String> filterSet(Set<String> inputDataset, BloomFilter<CharSequence> bloomFilter){
        log.debug("Called filterPanSet()");
        Set<String> resultSet = new HashSet<>();
        for(String s : inputDataset){
            if(bloomFilter.mightContain(s))
                resultSet.add(s);
        }
        return resultSet;
    }
}