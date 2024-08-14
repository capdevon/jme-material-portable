package com.test;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.plugins.JsonMaterialLoader;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Spatial;

/**
 * 
 * @author capdevon
 */
public class Test_ModelViewer extends SimpleApplication {

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        Test_ModelViewer app = new Test_ModelViewer();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        assetManager.registerLoader(JsonMaterialLoader.class, "json");
        
        Spatial sp = assetManager.loadModel("Models/YBot/YBot.j3o");
        rootNode.attachChild(sp);
        
        AmbientLight al = new AmbientLight();
        rootNode.addLight(al);
        
        DirectionalLight dl = new DirectionalLight();
        rootNode.addLight(dl);
        
        flyCam.setMoveSpeed(25f);
        flyCam.setDragToRotate(true);
        viewPort.setBackgroundColor(ColorRGBA.DarkGray);
    }

}
