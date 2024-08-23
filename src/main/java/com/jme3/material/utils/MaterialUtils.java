package com.jme3.material.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.jme3.material.MatParam;

/**
 * 
 * @author capdevon
 */
public class MaterialUtils {
    
    /**
     * Sorts a collection of material parameters.
     *
     * <p>This method takes a collection of material parameters, sorts them first by their type
     * and then by their name, and returns the sorted list.</p>
     *
     * @param params the collection of material parameters to be sorted
     * @return a list of sorted material parameters
     */
    public static List<MatParam> sortMatParams(Collection<MatParam> params) {
        List<MatParam> allParams = new ArrayList<>(params);

        // sort by type then name
        allParams.sort((a, b) -> {
            int type = a.getVarType().compareTo(b.getVarType());
            if (type == 0) {
                int name = a.getName().compareTo(b.getName());
                if (name == 0) {
                    return type;
                } else {
                    return name;
                }
            }
            return type;
        });
        return allParams;
    }

}
