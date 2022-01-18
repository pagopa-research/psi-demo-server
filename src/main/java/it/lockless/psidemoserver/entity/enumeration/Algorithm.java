package it.lockless.psidemoserver.entity.enumeration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Algorithm {
    DH(2048,4096),
    RSA(2048,4096);

    private final List<Integer> supportedKeySize;

    public List<Integer> getSupportedKeySize(){
        return new ArrayList<Integer>(supportedKeySize);
    }

    private Algorithm(Integer ... supportedKeySize){
        this.supportedKeySize= Arrays.asList(supportedKeySize);
    }


}
