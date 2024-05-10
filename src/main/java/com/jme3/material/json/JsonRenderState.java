package com.jme3.material.json;

import java.util.Arrays;

import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.RenderState.FaceCullMode;

public class JsonRenderState {
    
    public FaceCullMode faceCull;
    public boolean depthWrite;
    public boolean colorWrite;
    public float[] polyOffset;
    public boolean depthTest;
    public BlendMode blend;
    public boolean wireframe;

    @Override
    public String toString() {
        return "AdditionalRenderState [faceCull=" + faceCull 
                + ", depthWrite=" + depthWrite 
                + ", colorWrite=" + colorWrite 
                + ", polyOffset=" + Arrays.toString(polyOffset) 
                + ", depthTest=" + depthTest 
                + ", blend=" + blend
                + ", wireframe=" + wireframe 
                + "]";
    }

}