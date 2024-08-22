package com.jme3.material.exporter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import com.jme3.material.Materials;

/**
 * This class processes material definition asset names by potentially replacing
 * them with new names based on a pre-defined mapping.
 */
public class MatDefNameProcessor implements UnaryOperator<String> {

    public enum Format { JSON, YAML }
    
    private Map<String, String> map = new HashMap<>();

    /**
     * Creates a new MatDefNameProcessor instance using the provided format.
     * 
     * @param format The format to be used for generating the suffix.
     */
    public MatDefNameProcessor(Format format) {
        String ext = "." + format.name().toLowerCase();
        map.put("Common/MatDefs/Light/Lighting.j3md", "MatDefs/New/Lighting" + ext);
        map.put("Common/MatDefs/Light/PBRLighting.j3md", "MatDefs/New/PBRLighting" + ext);
        map.put("Common/MatDefs/Misc/Unshaded.j3md", "MatDefs/New/Unshaded" + ext);
    }

    @Override
    public String apply(String assetName) {
        return map.getOrDefault(assetName, assetName);
    }

}