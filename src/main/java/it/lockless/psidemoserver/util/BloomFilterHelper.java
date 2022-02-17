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

/**
 * Utility class that allows to generate and serialize/deserialize Bloom Filters.
 */

@SuppressWarnings("UnstableApiUsage")
public class BloomFilterHelper {

    private static final Logger log = LoggerFactory.getLogger(BloomFilterHelper.class);

    private BloomFilterHelper() {}

    /**
     * Generates a Bloom Filter from the current set of elements.
     * @param dataSet                   dataset on which compute the Bloom Filter
     * @param falsePositiveProbability  false positive probability characterizing the produced Bloom Filter
     * @return the computed bloom filter
     */
    public static BloomFilter<CharSequence> createBloomFilterByteArrayFromSet(Set<String> dataSet, Double falsePositiveProbability){
        log.debug("Called createBloomFilterByteArrayFromStringSet()");
        BloomFilter<CharSequence> bloomFilter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), dataSet.size(), falsePositiveProbability);
        for(String s : dataSet){
            bloomFilter.put(s);
        }
        return bloomFilter;
    }

    /**
     * Translates the Bloom Filter in a serializable shape.
     * @param bloomFilter   not serialized Bloom Filter
     * @return an array of bytes representing a serialized bloom filter
     */
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

    /**
     * Retrieves a Bloom Filter starting from an array of bytes.
     * @param bloomFilterByteArray an array of bytes representing a serialized Bloom Filter
     * @return the Bloom Filter
     */
    static BloomFilter<CharSequence> getBloomFilterFromByteArray(byte[] bloomFilterByteArray){
        log.debug("Called getBloomFilterFromByteArray()");
        InputStream inputStream = new ByteArrayInputStream(bloomFilterByteArray);
        try {
            return  BloomFilter.readFrom(inputStream, Funnels.stringFunnel(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new CustomRuntimeException("Cannot deserialize the Bloom Filter");
        }
    }

    /**
     * Applies the Bloom Filter to a set of elements.
     * @param inputDataset input dataset to be filtered
     * @param bloomFilter Bloom Filter used to be applied
     * @return the set filtered with the Bloom Filter
     */
    static Set<String> filterSet(Set<String> inputDataset, BloomFilter<CharSequence> bloomFilter){
        log.debug("Called filterPanSet()");
        Set<String> resultSet = new HashSet<>();
        for(String s : inputDataset){
            if(bloomFilter.mightContain(s))
                resultSet.add(s);
        }
        return resultSet;
    }
}