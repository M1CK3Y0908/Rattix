package client.m1ck3y.rattix.core;

import client.m1ck3y.rattix.util.EarlyDisplayUtil;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.8.9")
@IFMLLoadingPlugin.Name("RattixLoadingPlugin")
@IFMLLoadingPlugin.SortingIndex(1001)
public class RattixLoadingPlugin implements IFMLLoadingPlugin {

    static {
        // 在 CorePlugin 被类加载器装载的最早时刻立即执行
        EarlyDisplayUtil.applyEarly();
    }

    public RattixLoadingPlugin() {
        EarlyDisplayUtil.applyEarly();
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        EarlyDisplayUtil.applyEarly();
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
