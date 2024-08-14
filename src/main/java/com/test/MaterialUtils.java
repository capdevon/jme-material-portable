package com.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.jme3.material.MatParam;
import com.jme3.material.MatParamTexture;
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.material.RenderState;
import com.jme3.material.TechniqueDef;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.shader.UniformBinding;

/**
 * 
 * @author capdevon
 */
public class MaterialUtils {
    
    private MaterialUtils() {}
    
    /**
     * @param mat
     */
    public static void print(Material mat) {
        System.out.println(mat);
        System.out.println("MaterialParameters:");
        for (MatParam param : mat.getParams()) {
            System.out.println("      " + param);
        }
        
        System.out.println("AdditionalRenderState:");
        RenderState renderState = mat.getAdditionalRenderState();
        BlendMode blendMode = renderState.getBlendMode();
        FaceCullMode faceCullMode = renderState.getFaceCullMode();

        if (blendMode != RenderState.BlendMode.Off) {
            System.out.println("      Blend " + blendMode.name());
        }
        if (faceCullMode != RenderState.FaceCullMode.Back) {
            System.out.println("      FaceCull " + faceCullMode.name());
        }
        if (renderState.isWireframe()) {
            System.out.println("      Wireframe On");
        }
        if (!renderState.isDepthTest()) {
            System.out.println("      DepthTest Off");
        }
        if (!renderState.isDepthWrite()) {
            System.out.println("      DepthWrite Off");
        }
        if (!renderState.isColorWrite()) {
            System.out.println("      ColorWrite Off");
        }

        float factor = renderState.getPolyOffsetFactor();
        float units = renderState.getPolyOffsetUnits();

        if (factor != 0 || units != 0) {
            System.out.println("      PolyOffset " + factor + " " + units);
        }
    }
    
    /**
     * @param matDef
     */
    public static void print(MaterialDef matDef) {
        System.out.println(matDef);

        StringBuilder sb = new StringBuilder();
        sb.append("name : ").append(matDef.getName()).append("\n");
        Collection<MatParam> matParams = matDef.getMaterialParams();
        
        for (MatParam param : matParams) {
            sb.append(param);
            if (param instanceof MatParamTexture) {
                sb.append(" ").append(((MatParamTexture) param).getColorSpace());
            }
            sb.append("\n");
        }
        System.out.println(sb.toString() + "\n");

        for (String defName : matDef.getTechniqueDefsNames()) {
            System.out.println("____" + defName + "____");
            
            List<TechniqueDef> defs = matDef.getTechniqueDefs(defName);
            for (TechniqueDef technique : defs) {
                System.out.println(technique);

                // World params
                if (!technique.getWorldBindings().isEmpty()) {
                    for (UniformBinding uniformBinding : technique.getWorldBindings()) {
                        System.out.println("\t" + uniformBinding.toString());
                    }
                }

                // Defines
                if (technique.getDefineNames().length != 0) {
                    for (int i = 0; i < technique.getDefineNames().length; i++) {
                        String matParamName = getMatParamNameForDefineId(technique, matParams, i);
                        if (matParamName != null) {
                            String defineName = technique.getDefineNames()[i];
                            System.out.println(defineName + ": " + matParamName);
                        }
                    }
                }
            }
        }
    }
    
    private static String getMatParamNameForDefineId(TechniqueDef techniqueDef, Collection<MatParam> matParams, int defineId) {
        for (MatParam matParam : matParams) {
            Integer id = techniqueDef.getShaderParamDefineId(matParam.getName());
            if (id != null && id == defineId) {
                return matParam.getName();
            }
        }
        return null;
    }
    
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
