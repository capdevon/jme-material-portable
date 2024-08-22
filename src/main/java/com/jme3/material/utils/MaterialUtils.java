package com.jme3.material.utils;

import java.util.ArrayList;
import java.util.List;

import com.jme3.material.MatParam;
import com.jme3.material.Material;

/**
 * 
 * @author capdevon
 */
public class MaterialUtils {
    
    public static List<MatParam> sortMatParams(Material mat) {
        List<MatParam> allParams = new ArrayList<>();

        // get all material parameters declared in this material.
        for (MatParam param : mat.getParams()) {
            allParams.add(param);
        }

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
