package com.test;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.YamlMaterialKey;
import com.jme3.material.MatParam;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.material.plugins.YamlMaterialLoader;
import com.jme3.system.JmeContext;

/**
 * 
 * @author capdevon
 */
public class Test_YamlMaterialLoader extends SimpleApplication {

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        Test_YamlMaterialLoader app = new Test_YamlMaterialLoader();
        app.start(JmeContext.Type.Headless);
    }

    @Override
    public void simpleInitApp() {
        assetManager.registerLoader(YamlMaterialLoader.class, "yaml");

        YamlMaterialKey key = new YamlMaterialKey("MatDefs/New/Material.yaml");
        Material mat = assetManager.loadAsset(key);
        printDebug(mat);
        
        stop();
    }

    /**
     * @param mat
     */
    private void printDebug(Material mat) {
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

}
