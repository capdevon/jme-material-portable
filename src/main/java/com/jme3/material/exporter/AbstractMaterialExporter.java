package com.jme3.material.exporter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.UnaryOperator;

import com.jme3.asset.TextureKey;
import com.jme3.material.MatParam;
import com.jme3.material.MatParamTexture;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.material.json.JsonMatParam;
import com.jme3.material.json.JsonMaterial;
import com.jme3.material.json.JsonRenderState;
import com.jme3.material.json.JsonTexture;
import com.jme3.material.utils.MaterialUtils;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.shader.VarType;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;

/**
 * 
 * @author capdevon
 */
public abstract class AbstractMaterialExporter {
    
    protected UnaryOperator<String> matDefNameProcessor = UnaryOperator.identity();

    public void setMatDefNameProcessor(UnaryOperator<String> matDefNameProcessor) {
        this.matDefNameProcessor = matDefNameProcessor;
    }
    
    public abstract void save(Material material, File file) throws IOException;

    protected JsonMaterial toJson(Material material) {
        JsonMaterial mat = new JsonMaterial();
        mat.setName(material.getName());
        
        String defName = material.getMaterialDef().getAssetName();
        mat.setDef(matDefNameProcessor.apply(defName));

        List<JsonMatParam> parameters = toJson(MaterialUtils.sortMatParams(material));
        mat.setMaterialParameters(parameters);

        JsonRenderState renderState = toJson(material.getAdditionalRenderState());
        mat.setAdditionalRenderState(renderState);
        return mat;
    }

    protected List<JsonMatParam> toJson(Collection<MatParam> params) {
        List<JsonMatParam> parameters = new ArrayList<>();
        for (MatParam param : params) {
            
            JsonMatParam json = new JsonMatParam();
            json.name = param.getName();
            
            if (param instanceof MatParamTexture) {
                json.texture = formatMatParamTexture((MatParamTexture) param);
            } else {
                json.value = formatMatParam(param);
            }
            
            parameters.add(json);
        }
        return parameters;
    }

    protected JsonRenderState toJson(RenderState rs) {
        JsonRenderState json = new JsonRenderState();
        RenderState defRs = RenderState.DEFAULT;
        
        if (rs.getBlendMode() != defRs.getBlendMode()) {
            json.blend = rs.getBlendMode();
        }
        if (rs.isWireframe() != defRs.isWireframe()) {
            json.wireframe = rs.isWireframe();
        }
        if (rs.getFaceCullMode() != defRs.getFaceCullMode()) {
            json.faceCull = rs.getFaceCullMode();
        }
        if (rs.isDepthWrite() != defRs.isDepthWrite()) {
            json.depthWrite = rs.isDepthWrite();
        }
        if (rs.isDepthTest() != defRs.isDepthTest()) {
            json.depthTest = rs.isDepthTest();
        }
//        if (rs.getBlendEquation() != defRs.getBlendEquation()) {
//            json.addProperty("blendEquation", rs.getBlendEquation().name());
//        }
//        if (rs.getBlendEquationAlpha() != defRs.getBlendEquationAlpha()) {
//            json.addProperty("blendEquationAlpha", rs.getBlendEquationAlpha().name());
//        }
        if (rs.isColorWrite() != defRs.isColorWrite()) {
            json.colorWrite = rs.isColorWrite();
        }
        if (rs.getDepthFunc() != defRs.getDepthFunc()) {
            json.depthFunc = rs.getDepthFunc();
        }
        if (rs.getLineWidth() != defRs.getLineWidth()) {
            json.lineWidth = rs.getLineWidth();
        }
        
        if (rs.getPolyOffsetFactor() != defRs.getPolyOffsetFactor()
                || rs.getPolyOffsetUnits() != defRs.getPolyOffsetUnits()) {
            json.polyOffset = new float[] { rs.getPolyOffsetFactor(), rs.getPolyOffsetUnits() };
        }
        
        return json;
    }
    
    protected Object formatMatParam(MatParam param) {
        VarType type = param.getVarType();
        Object val = param.getValue();
        
        switch (type) {
            case Boolean:
            case Float:
            case Int:
                return val;
                
            case Vector2:
                Vector2f vec2 = (Vector2f) val;
                return vec2.toArray(null);
                
            case Vector3:
                Vector3f vec3 = (Vector3f) val;
                return vec3.toArray(null);
                
            case Vector4:
                if (val instanceof Vector4f) {
                    Vector4f vec4 = (Vector4f) val;
                    return vec4.toArray(null);

                } else if (val instanceof ColorRGBA) {
                    ColorRGBA color = (ColorRGBA) val;
                    return color.toArray(null);

                } else {
                    throw new UnsupportedOperationException("Unexpected Vector4 type: " + val);
                }

            default:
                return null; // parameter type not supported in J3M
        }
    }

    protected JsonTexture formatMatParamTexture(MatParamTexture param) {
        JsonTexture json = new JsonTexture();
        Texture tex = (Texture) param.getValue();
        if (tex != null) {
            
            TextureKey key = (TextureKey) tex.getKey();
            if (key != null) {
                json.path = key.getName();
                
                if (key.isFlipY()) {
                    json.flipY = true;
                }
            }

            Texture.WrapAxis[] axis = { Texture.WrapAxis.S, Texture.WrapAxis.T, Texture.WrapAxis.R };
            for (int i = 0; i < axis.length; i++) {
                WrapMode wrapMode = formatWrapMode(tex, axis[i]);
                if (wrapMode != null) {
                    switch (axis[i]) {
                        case S:
                            json.wrapS = wrapMode;
                            break;
                        case T:
                            json.wrapT = wrapMode;
                            break;
                        case R:
                            json.wrapR = wrapMode;
                            break;
                    }
                }
            }

            // Min and Mag filter
            if (tex.getMinFilter() != Texture.MinFilter.Trilinear) {
                json.minFilter = tex.getMinFilter();
            }
            if (tex.getMagFilter() != Texture.MagFilter.Bilinear) {
                json.magFilter = tex.getMagFilter();
            }
        }

        return json;
    }

    protected WrapMode formatWrapMode(Texture texVal, Texture.WrapAxis axis) {
        WrapMode mode;
        try {
            mode = texVal.getWrap(axis);
        } catch (IllegalArgumentException e) {
            // this axis doesn't exist on the texture
            return null;
        }
        if (mode != WrapMode.EdgeClamp) {
            return mode;
        }
        return null;
    }
    
}
