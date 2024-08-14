package com.jme3.asset;

import com.jme3.asset.cache.AssetCache;
import com.jme3.asset.cache.WeakRefCloneAssetCache;
import com.jme3.material.MaterialDef;

/**
 * 
 * @author capdevon
 */
@Deprecated
public class YamlMaterialDefKey extends AssetKey<MaterialDef> {

    public YamlMaterialDefKey() {
        super();
    }

    public YamlMaterialDefKey(String name) {
        super(name);
    }

    @Override
    public Class<? extends AssetCache> getCacheType() {
        return WeakRefCloneAssetCache.class;
    }

    @Override
    public Class<? extends AssetProcessor> getProcessorType() {
        return CloneableAssetProcessor.class;
    }

}