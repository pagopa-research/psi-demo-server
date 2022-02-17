package it.lockless.psidemoserver.entity.enumeration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * Contains the algorithms supported by the server wit the respective supported key sizes.
 */

public enum Algorithm {
    DH(2048,4096),
    BS(2048,4096),
    ECBS(256,512),
    ECDH(256,512);

    private final List<Integer> supportedKeySize;

    public List<Integer> getSupportedKeySize(){
        return new ArrayList<>(supportedKeySize);
    }

    private Algorithm(Integer ... supportedKeySize){
        this.supportedKeySize= Arrays.asList(supportedKeySize);
    }


}
