package client.m1ck3y.rattix.injection;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.8.9")
@IFMLLoadingPlugin.Name("Rattix")
@IFMLLoadingPlugin.SortingIndex(1)
public class RattixLoadingPlugin implements IFMLLoadingPlugin {

    static {
        injectSelfToSystemClassLoader();
    }

    private static void injectSelfToSystemClassLoader() {
        try {
            URL url = RattixLoadingPlugin.class.getProtectionDomain().getCodeSource().getLocation();
            if (url != null) {
                // 1. Inject into SystemClassLoader (AppClassLoader) so IMixinService and Mixin classes can be loaded when excluded by LaunchClassLoader
                ClassLoader sysLoader = ClassLoader.getSystemClassLoader();
                addUrlToClassLoader(sysLoader, url);

                // 2. Inject into LaunchClassLoader parent if different
                try {
                    if (Launch.classLoader != null) {
                        ClassLoader parent = Launch.classLoader.getClass().getClassLoader();
                        if (parent != null && parent != sysLoader) {
                            addUrlToClassLoader(parent, url);
                        }
                        ClassLoader launchParent = Launch.classLoader.getParent();
                        if (launchParent != null && launchParent != sysLoader) {
                            addUrlToClassLoader(launchParent, url);
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
        } catch (Throwable t) {
            System.err.println("[Rattix] Failed to inject self to system class loader:");
            t.printStackTrace();
        }
    }

    private static void addUrlToClassLoader(ClassLoader classLoader, URL url) {
        if (classLoader == null || url == null) return;
        try {
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(classLoader, url);
        } catch (Throwable ignored) {
            try {
                Field ucpField = classLoader.getClass().getDeclaredField("ucp");
                ucpField.setAccessible(true);
                Object ucp = ucpField.get(classLoader);
                Method addURL = ucp.getClass().getDeclaredMethod("addURL", URL.class);
                addURL.setAccessible(true);
                addURL.invoke(ucp, url);
            } catch (Throwable ignored2) {
            }
        }
    }

    private static void configureMixinEnvironment() {
        try {
            MixinBootstrap.init();
            Mixins.addConfiguration("mixins.rattix.json");

            for (MixinEnvironment.Phase phase : new MixinEnvironment.Phase[]{
                    MixinEnvironment.Phase.PREINIT,
                    MixinEnvironment.Phase.INIT,
                    MixinEnvironment.Phase.DEFAULT
            }) {
                try {
                    MixinEnvironment env = MixinEnvironment.getEnvironment(phase);
                    if (env != null) {
                        env.setSide(MixinEnvironment.Side.CLIENT);
                        env.setObfuscationContext("searge");
                    }
                } catch (Throwable ignored) {}
            }

            MixinEnvironment defaultEnv = MixinEnvironment.getDefaultEnvironment();
            if (defaultEnv != null) {
                defaultEnv.setSide(MixinEnvironment.Side.CLIENT);
                defaultEnv.setObfuscationContext("searge");
            }
        } catch (Throwable t) {
            System.err.println("[Rattix] Failed to configure Mixin environment:");
            t.printStackTrace();
        }
    }

    public RattixLoadingPlugin() {
        injectSelfToSystemClassLoader();
        configureMixinEnvironment();
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
        configureMixinEnvironment();
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
