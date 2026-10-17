package client.m1ck3y.rattix.utils.shader;

import client.m1ck3y.rattix.RattixMod;
import client.m1ck3y.rattix.module.modules.MenuShader;
import client.m1ck3y.rattix.utils.FileManager;
import client.m1ck3y.rattix.utils.RenderUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GLContext;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShaderManager {
    private static volatile ShaderManager instance;

    private final Minecraft mc = Minecraft.getMinecraft();
    private final List<String> shaderList = new ArrayList<>();
    private String currentShader = "retrowave";
    private int currentIndex = 0;

    // Performance & Tuning Settings
    private double resolution = 100.0; // 0 - 200, default 100
    private double fpsLimit = 0.0;     // 0 - 600, 0 = VSync / uncapped
    private double speed = 1.0;        // 0.0 - 10.0, default 1.0

    // Animation & Pacing
    private float accumulatedTime = 0.0f;
    private long lastTimeMs = System.currentTimeMillis();
    private long lastRenderTimeNs = 0;
    private boolean hasRenderedFrame = false;
    private boolean needsRedraw = true;

    // OpenGL & Framebuffer Objects
    private boolean glInitialized = false;
    private int vertexShaderId = 0;
    private int fragmentShaderId = 0;
    private int programId = 0;
    private Framebuffer shaderFbo = null;

    private int timeLoc = -1;
    private int resolutionLoc = -1;
    private int mouseLoc = -1;
    private int iMouseLoc = -1;
    private int zoomLoc = -1;
    private int alphaLoc = -1;
    private int speedLoc = -1;
    private int shiftLoc = -1;

    // UI State
    private boolean browserOpen = false;
    private boolean settingsOpen = false;
    private String searchQuery = "";
    private int scrollOffset = 0;
    private int draggingSlider = 0; // 0 = none, 1 = resolution, 2 = fps, 3 = speed

    // Toast
    private String toastText = null;
    private long toastEndTime = 0;

    private final File configJsonFile = new File(FileManager.RATTIX_DIR, "background.json");
    private final File configTxtFile = new File(FileManager.RATTIX_DIR, "background.txt");

    public static ShaderManager getInstance() {
        if (instance == null) {
            synchronized (ShaderManager.class) {
                if (instance == null) {
                    instance = new ShaderManager();
                }
            }
        }
        return instance;
    }

    private ShaderManager() {
        loadShaderList();
        loadConfig();
    }

    private void loadShaderList() {
        shaderList.clear();
        shaderList.add("Vanilla");

        // 1. Load from classpath resource: /assets/rattix/shaders/shaders.txt
        try (InputStream in = RattixMod.class.getResourceAsStream("/assets/rattix/shaders/shaders.txt")) {
            if (in != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !shaderList.contains(line) && !line.equalsIgnoreCase("Vanilla")) {
                        shaderList.add(line);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. Load custom shaders from rattix/shaders directory if present
        File customDir = new File(FileManager.RATTIX_DIR, "shaders");
        if (customDir.exists() && customDir.isDirectory()) {
            File[] files = customDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".frag"));
            if (files != null) {
                for (File file : files) {
                    String baseName = file.getName().substring(0, file.getName().length() - 5);
                    if (!shaderList.contains(baseName)) {
                        shaderList.add(baseName);
                    }
                }
            }
        }

        // Fallback default list if somehow empty
        if (shaderList.size() <= 1) {
            String[] fallbacks = new String[] {
                    "retrowave", "Augustus1", "Augustus2", "auroras", "blackhole",
                    "galaxy", "space", "matrix", "singularity", "water", "star"
            };
            for (String f : fallbacks) {
                if (!shaderList.contains(f)) {
                    shaderList.add(f);
                }
            }
        }

        updateCurrentIndex();
    }

    private void loadConfig() {
        try {
            // First try modern background.json
            if (configJsonFile.exists()) {
                try (FileReader fr = new FileReader(configJsonFile)) {
                    JsonObject obj = new JsonParser().parse(fr).getAsJsonObject();
                    if (obj.has("shader")) {
                        String s = obj.get("shader").getAsString();
                        for (String item : shaderList) {
                            if (item.equalsIgnoreCase(s)) {
                                this.currentShader = item;
                                break;
                            }
                        }
                    }
                    if (obj.has("resolution")) {
                        this.resolution = Math.max(0.0, Math.min(200.0, obj.get("resolution").getAsDouble()));
                    }
                    if (obj.has("fpsLimit")) {
                        this.fpsLimit = Math.max(0.0, Math.min(600.0, obj.get("fpsLimit").getAsDouble()));
                    }
                    if (obj.has("speed")) {
                        this.speed = Math.max(0.0, Math.min(10.0, obj.get("speed").getAsDouble()));
                    }
                    updateCurrentIndex();
                    syncToModule();
                    return;
                }
            }

            // Fallback to background.txt
            if (configTxtFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(configTxtFile));
                String line = reader.readLine();
                reader.close();
                if (line != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        for (String s : shaderList) {
                            if (s.equalsIgnoreCase(line)) {
                                this.currentShader = s;
                                updateCurrentIndex();
                                syncToModule();
                                return;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (shaderList.contains("retrowave")) {
            this.currentShader = "retrowave";
        } else if (shaderList.size() > 1) {
            this.currentShader = shaderList.get(1);
        } else {
            this.currentShader = "Vanilla";
        }
        updateCurrentIndex();
        syncToModule();
    }

    public void saveConfig() {
        try {
            if (!FileManager.RATTIX_DIR.exists()) {
                FileManager.RATTIX_DIR.mkdirs();
            }

            // Save JSON
            JsonObject obj = new JsonObject();
            obj.addProperty("shader", this.currentShader);
            obj.addProperty("resolution", this.resolution);
            obj.addProperty("fpsLimit", this.fpsLimit);
            obj.addProperty("speed", this.speed);

            try (PrintWriter pw = new PrintWriter(new FileWriter(configJsonFile))) {
                pw.println(obj.toString());
            }

            // Save legacy TXT
            try (PrintWriter writer = new PrintWriter(new FileWriter(configTxtFile))) {
                writer.println(this.currentShader);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void syncToModule() {
        MenuShader module = MenuShader.getInstance();
        if (module != null) {
            module.syncFromShaderManager(this.resolution, this.fpsLimit, this.speed);
        }
    }

    private void updateCurrentIndex() {
        this.currentIndex = 0;
        for (int i = 0; i < shaderList.size(); i++) {
            if (shaderList.get(i).equalsIgnoreCase(this.currentShader)) {
                this.currentIndex = i;
                break;
            }
        }
    }

    public boolean isShaderActive() {
        return !"Vanilla".equalsIgnoreCase(currentShader);
    }

    public String getCurrentShader() {
        return currentShader;
    }

    public List<String> getShaderList() {
        return Collections.unmodifiableList(shaderList);
    }

    public double getResolution() {
        return resolution;
    }

    public void setResolution(double resolution) {
        this.resolution = Math.max(0.0, Math.min(200.0, resolution));
        this.needsRedraw = true;
        saveConfig();
        syncToModule();
    }

    public double getFpsLimit() {
        return fpsLimit;
    }

    public void setFpsLimit(double fpsLimit) {
        this.fpsLimit = Math.max(0.0, Math.min(600.0, fpsLimit));
        saveConfig();
        syncToModule();
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = Math.max(0.0, Math.min(10.0, speed));
        saveConfig();
        syncToModule();
    }

    public void nextShader() {
        if (shaderList.isEmpty()) return;
        currentIndex = (currentIndex + 1) % shaderList.size();
        setShaderByIndex(currentIndex);
    }

    public void prevShader() {
        if (shaderList.isEmpty()) return;
        currentIndex = (currentIndex - 1 + shaderList.size()) % shaderList.size();
        setShaderByIndex(currentIndex);
    }

    public void setShader(String name) {
        for (int i = 0; i < shaderList.size(); i++) {
            if (shaderList.get(i).equalsIgnoreCase(name)) {
                setShaderByIndex(i);
                return;
            }
        }
    }

    private void setShaderByIndex(int index) {
        this.currentIndex = index;
        this.currentShader = shaderList.get(index);
        this.needsRedraw = true;
        saveConfig();
        showToast("Shader: " + formatShaderName(this.currentShader) + "  [" + (index + 1) + "/" + shaderList.size() + "]");
        playClickSound();

        compileActiveShader();
    }

    private void showToast(String text) {
        this.toastText = text;
        this.toastEndTime = System.currentTimeMillis() + 2500;
    }

    private void playClickSound() {
        try {
            mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        } catch (Throwable ignored) {
        }
    }

    private String readShaderSource(String shaderName) {
        // 1. Try rattix/shaders/<name>.frag
        File customFile = new File(FileManager.RATTIX_DIR, "shaders/" + shaderName + ".frag");
        if (customFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(customFile))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                return sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 2. Try classpath
        try (InputStream in = RattixMod.class.getResourceAsStream("/assets/rattix/shaders/" + shaderName + ".frag")) {
            if (in == null) return null;
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String readVertexSource() {
        try (InputStream in = RattixMod.class.getResourceAsStream("/assets/rattix/shaders/shader.vert")) {
            if (in == null) {
                return "#version 120\nvoid main() {\n    gl_Position = ftransform();\n}\n";
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        } catch (Exception e) {
            return "#version 120\nvoid main() {\n    gl_Position = ftransform();\n}\n";
        }
    }

    public synchronized void compileActiveShader() {
        if (!GLContext.getCapabilities().OpenGL20) {
            return;
        }

        if ("Vanilla".equalsIgnoreCase(currentShader)) {
            unloadCurrentProgram();
            return;
        }

        // Initialize vertex shader if needed
        if (vertexShaderId == 0) {
            String vertSrc = readVertexSource();
            vertexShaderId = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
            GL20.glShaderSource(vertexShaderId, vertSrc);
            GL20.glCompileShader(vertexShaderId);
            if (GL20.glGetShaderi(vertexShaderId, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
                System.err.println("[Rattix] Failed to compile vertex shader:");
                System.err.println(GL20.glGetShaderInfoLog(vertexShaderId, 4096));
                GL20.glDeleteShader(vertexShaderId);
                vertexShaderId = 0;
                return;
            }
        }

        String fragSrc = readShaderSource(currentShader);
        if (fragSrc == null) {
            System.err.println("[Rattix] Could not find shader file: " + currentShader);
            return;
        }

        int fragId = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragId, fragSrc);
        GL20.glCompileShader(fragId);

        if (GL20.glGetShaderi(fragId, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            System.err.println("[Rattix] Failed to compile fragment shader: " + currentShader);
            System.err.println(GL20.glGetShaderInfoLog(fragId, 4096));
            GL20.glDeleteShader(fragId);
            return;
        }

        int newProg = GL20.glCreateProgram();
        GL20.glAttachShader(newProg, vertexShaderId);
        GL20.glAttachShader(newProg, fragId);
        GL20.glLinkProgram(newProg);

        if (GL20.glGetProgrami(newProg, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            System.err.println("[Rattix] Failed to link shader program: " + currentShader);
            System.err.println(GL20.glGetProgramInfoLog(newProg, 4096));
            GL20.glDeleteShader(fragId);
            GL20.glDeleteProgram(newProg);
            return;
        }

        // Swap program
        unloadCurrentProgram();
        this.programId = newProg;
        this.fragmentShaderId = fragId;

        // Fetch uniform locations
        this.timeLoc = GL20.glGetUniformLocation(programId, "time");
        this.resolutionLoc = GL20.glGetUniformLocation(programId, "resolution");
        this.mouseLoc = GL20.glGetUniformLocation(programId, "mouse");
        this.iMouseLoc = GL20.glGetUniformLocation(programId, "iMouse");
        this.zoomLoc = GL20.glGetUniformLocation(programId, "zoom");
        this.alphaLoc = GL20.glGetUniformLocation(programId, "alpha");
        this.speedLoc = GL20.glGetUniformLocation(programId, "speed");
        this.shiftLoc = GL20.glGetUniformLocation(programId, "shift");

        this.accumulatedTime = 0.0f;
        this.lastTimeMs = System.currentTimeMillis();
        this.lastRenderTimeNs = 0;
        this.hasRenderedFrame = false;
        this.needsRedraw = true;
        this.glInitialized = true;
    }

    private void unloadCurrentProgram() {
        if (programId != 0) {
            GL20.glUseProgram(0);
            if (fragmentShaderId != 0) {
                GL20.glDetachShader(programId, fragmentShaderId);
                GL20.glDeleteShader(fragmentShaderId);
                fragmentShaderId = 0;
            }
            if (vertexShaderId != 0) {
                GL20.glDetachShader(programId, vertexShaderId);
            }
            GL20.glDeleteProgram(programId);
            programId = 0;
        }
    }

    /**
     * Highly optimized render pipeline:
     * 1. Resolution Scaling: renders to an off-screen FBO at scaled resolution (0-200%).
     * 2. Frame Limiter: only executes GLSL fragment shader when target frame interval has elapsed.
     * 3. Frame Caching: blits cached FBO texture on intervening frames with virtually zero GPU cost!
     * 4. Speed Multiplier: smoothly steps accumulated time according to user speed (0.0x - 10.0x).
     */
    public synchronized void render(int screenWidth, int screenHeight, int mouseX, int mouseY) {
        if (!isShaderActive()) {
            return;
        }

        if (!glInitialized || programId == 0) {
            compileActiveShader();
            if (programId == 0) return;
        }

        // Time accumulation with speed multiplier and lag protection
        long nowMs = System.currentTimeMillis();
        float deltaSec = (nowMs - lastTimeMs) / 1000.0f;
        lastTimeMs = nowMs;
        if (deltaSec > 0.2f) deltaSec = 0.2f;
        if (speed > 0.0) {
            accumulatedTime += deltaSec * (float) speed;
        }

        float dispW = (float) Math.max(1, mc.displayWidth);
        float dispH = (float) Math.max(1, mc.displayHeight);

        // Compute resolution scaling (clamping 0 to minimum 10% to prevent 0-sized FBO crash)
        float resScale = (float) (resolution <= 0.0 ? 0.10 : (resolution / 100.0));
        int renderW = Math.max(16, (int) (dispW * resScale));
        int renderH = Math.max(16, (int) (dispH * resScale));

        boolean canUseFbo = OpenGlHelper.isFramebufferEnabled();

        // Check if FBO needs creation or resize
        if (canUseFbo) {
            if (shaderFbo == null || shaderFbo.framebufferWidth != renderW || shaderFbo.framebufferHeight != renderH) {
                if (shaderFbo != null) {
                    shaderFbo.deleteFramebuffer();
                }
                shaderFbo = new Framebuffer(renderW, renderH, false);
                shaderFbo.setFramebufferFilter(GL11.GL_LINEAR);
                needsRedraw = true;
            }
        }

        // Frame rate limiting logic
        long nowNs = System.nanoTime();
        boolean shouldUpdateShader = false;
        if (fpsLimit <= 0.0) {
            shouldUpdateShader = true;
        } else {
            long intervalNs = (long) (1_000_000_000.0 / fpsLimit);
            if (nowNs - lastRenderTimeNs >= intervalNs || !hasRenderedFrame || needsRedraw) {
                shouldUpdateShader = true;
                lastRenderTimeNs = nowNs;
            }
        }

        if (needsRedraw) {
            shouldUpdateShader = true;
            needsRedraw = false;
        }

        // If FBO is supported, render shader to FBO when updating, then draw FBO to screen
        if (canUseFbo && shaderFbo != null) {
            if (shouldUpdateShader) {
                // Render into offscreen FBO
                shaderFbo.bindFramebuffer(true);

                GlStateManager.pushMatrix();
                GlStateManager.matrixMode(GL11.GL_PROJECTION);
                GlStateManager.pushMatrix();
                GlStateManager.loadIdentity();
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                GlStateManager.pushMatrix();
                GlStateManager.loadIdentity();

                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GlStateManager.disableAlpha();
                GlStateManager.disableCull();
                GlStateManager.disableDepth();
                GlStateManager.depthMask(false);

                GL20.glUseProgram(programId);

                if (timeLoc != -1) GL20.glUniform1f(timeLoc, accumulatedTime);
                if (resolutionLoc != -1) GL20.glUniform2f(resolutionLoc, (float) renderW, (float) renderH);

                float mousePx = (float) Mouse.getX();
                float mousePy = (float) Mouse.getY();
                if (mouseLoc != -1) GL20.glUniform2f(mouseLoc, mousePx / dispW, mousePy / dispH);
                if (iMouseLoc != -1) GL20.glUniform4f(iMouseLoc, mousePx * resScale, mousePy * resScale, 0.0f, 0.0f);
                if (zoomLoc != -1) GL20.glUniform1f(zoomLoc, 1.0f);
                if (alphaLoc != -1) GL20.glUniform1f(alphaLoc, 1.0f);
                if (speedLoc != -1) GL20.glUniform2f(speedLoc, 1.0f, 1.0f);
                if (shiftLoc != -1) GL20.glUniform1f(shiftLoc, 0.0f);

                GL11.glBegin(GL11.GL_QUADS);
                GL11.glVertex2f(-1.0f, -1.0f);
                GL11.glVertex2f(1.0f, -1.0f);
                GL11.glVertex2f(1.0f, 1.0f);
                GL11.glVertex2f(-1.0f, 1.0f);
                GL11.glEnd();

                GL20.glUseProgram(0);

                GlStateManager.enableDepth();
                GlStateManager.depthMask(true);
                GlStateManager.enableAlpha();
                GlStateManager.enableCull();

                GlStateManager.popMatrix();
                GlStateManager.matrixMode(GL11.GL_PROJECTION);
                GlStateManager.popMatrix();
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                GlStateManager.popMatrix();

                shaderFbo.unbindFramebuffer();

                // Re-bind Minecraft's main framebuffer and restore viewport
                if (mc.getFramebuffer() != null) {
                    mc.getFramebuffer().bindFramebuffer(true);
                }
                hasRenderedFrame = true;
            }

            // Blit FBO texture to the screen (bilinear filtered upscaling)
            GlStateManager.pushMatrix();
            GlStateManager.matrixMode(GL11.GL_PROJECTION);
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();

            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableAlpha();
            GlStateManager.disableCull();
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
            GlStateManager.enableTexture2D();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            shaderFbo.bindFramebufferTexture();

            GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(0.0F, 1.0F); GL11.glVertex2f(-1.0F, -1.0F);
            GL11.glTexCoord2f(1.0F, 1.0F); GL11.glVertex2f(1.0F, -1.0F);
            GL11.glTexCoord2f(1.0F, 0.0F); GL11.glVertex2f(1.0F, 1.0F);
            GL11.glTexCoord2f(0.0F, 0.0F); GL11.glVertex2f(-1.0F, 1.0F);
            GL11.glEnd();

            shaderFbo.unbindFramebufferTexture();

            GlStateManager.disableTexture2D();
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.enableAlpha();
            GlStateManager.enableCull();

            GlStateManager.popMatrix();
            GlStateManager.matrixMode(GL11.GL_PROJECTION);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            GlStateManager.popMatrix();

        } else {
            // Direct screen render fallback (if FBO is not supported)
            GlStateManager.pushMatrix();
            GlStateManager.matrixMode(GL11.GL_PROJECTION);
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();

            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableAlpha();
            GlStateManager.disableCull();
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);

            GL20.glUseProgram(programId);

            if (timeLoc != -1) GL20.glUniform1f(timeLoc, accumulatedTime);
            if (resolutionLoc != -1) GL20.glUniform2f(resolutionLoc, dispW, dispH);

            float mousePx = (float) Mouse.getX();
            float mousePy = (float) Mouse.getY();
            if (mouseLoc != -1) GL20.glUniform2f(mouseLoc, mousePx / dispW, mousePy / dispH);
            if (iMouseLoc != -1) GL20.glUniform4f(iMouseLoc, mousePx, mousePy, 0.0f, 0.0f);
            if (zoomLoc != -1) GL20.glUniform1f(zoomLoc, 1.0f);
            if (alphaLoc != -1) GL20.glUniform1f(alphaLoc, 1.0f);
            if (speedLoc != -1) GL20.glUniform2f(speedLoc, 1.0f, 1.0f);
            if (shiftLoc != -1) GL20.glUniform1f(shiftLoc, 0.0f);

            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2f(-1.0f, -1.0f);
            GL11.glVertex2f(1.0f, -1.0f);
            GL11.glVertex2f(1.0f, 1.0f);
            GL11.glVertex2f(-1.0f, 1.0f);
            GL11.glEnd();

            GL20.glUseProgram(0);

            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.enableAlpha();
            GlStateManager.enableCull();

            GlStateManager.popMatrix();
            GlStateManager.matrixMode(GL11.GL_PROJECTION);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            GlStateManager.popMatrix();
        }
    }

    public void renderUI(int mouseX, int mouseY, int screenWidth, int screenHeight) {
        FontRenderer fr = mc.fontRendererObj;
        if (fr == null) return;

        // Process active dragging for settings sliders
        if (settingsOpen && Mouse.isButtonDown(0) && draggingSlider != 0) {
            updateSliderDrag(mouseX);
        } else if (!Mouse.isButtonDown(0)) {
            draggingSlider = 0;
        }

        int pillX = 12;
        int pillY = 12;
        int pillW = 210;
        int pillH = 24;

        // Draw main pill capsule
        boolean hoverPill = mouseX >= pillX && mouseX <= pillX + pillW && mouseY >= pillY && mouseY <= pillY + pillH;
        int pillBg = hoverPill ? 0xE6141418 : 0xD0141418;
        int pillBorder = hoverPill ? 0x4538B6FF : 0x25FFFFFF;
        RenderUtil.drawRoundedRect(pillX, pillY, pillW, pillH, 6.0f, pillBg);
        RenderUtil.drawRoundedOutline(pillX, pillY, pillW, pillH, 6.0f, 1.0f, pillBorder);

        // Prev chevron button [ < ]
        int prevX = pillX + 3;
        int prevY = pillY + 3;
        int prevW = 18;
        int prevH = 18;
        boolean hoverPrev = mouseX >= prevX && mouseX <= prevX + prevW && mouseY >= prevY && mouseY <= prevY + prevH;
        if (hoverPrev) {
            RenderUtil.drawRoundedRect(prevX, prevY, prevW, prevH, 4.0f, 0x4038B6FF);
        }
        int prevCol = hoverPrev ? 0xFF38B6FF : 0xFFB0B0B8;
        fr.drawString("<", prevX + (prevW - fr.getStringWidth("<")) / 2, prevY + 5, prevCol);

        // Next chevron button [ > ]
        int nextX = pillX + pillW - 21;
        int nextY = pillY + 3;
        int nextW = 18;
        int nextH = 18;
        boolean hoverNext = mouseX >= nextX && mouseX <= nextX + nextW && mouseY >= nextY && mouseY <= nextY + nextH;
        if (hoverNext) {
            RenderUtil.drawRoundedRect(nextX, nextY, nextW, nextH, 4.0f, 0x4038B6FF);
        }
        int nextCol = hoverNext ? 0xFF38B6FF : 0xFFB0B0B8;
        fr.drawString(">", nextX + (nextW - fr.getStringWidth(">")) / 2, nextY + 5, nextCol);

        // Middle indicator dot
        int dotColor = isShaderActive() ? 0xFF38B6FF : 0xFF888888;
        RenderUtil.drawRoundedRect(pillX + 26, pillY + 9, 6, 6, 3.0f, dotColor);

        // Middle shader name
        String displayName = formatShaderName(currentShader);
        if (displayName.length() > 16) {
            displayName = displayName.substring(0, 15) + "…";
        }
        int midWidth = pillW - 48;
        int midCenterX = pillX + 24 + midWidth / 2;
        int textW = fr.getStringWidth(displayName);
        fr.drawString(displayName, midCenterX - textW / 2 + 4, pillY + 8, 0xFFFFFFFF);

        // 1. Browse / List modal button [ ≡ ]
        int listX = pillX + pillW + 6;
        int listY = pillY;
        int listW = 24;
        int listH = 24;
        boolean hoverList = mouseX >= listX && mouseX <= listX + listW && mouseY >= listY && mouseY <= listY + listH;
        int listBg = hoverList ? 0xE6202028 : 0xD0141418;
        int listBorder = hoverList ? 0xFF38B6FF : 0x25FFFFFF;
        RenderUtil.drawRoundedRect(listX, listY, listW, listH, 6.0f, listBg);
        RenderUtil.drawRoundedOutline(listX, listY, listW, listH, 6.0f, 1.0f, listBorder);
        String listIcon = "≡";
        int iconCol = hoverList ? 0xFF38B6FF : 0xFFB0B0B8;
        fr.drawString(listIcon, listX + (listW - fr.getStringWidth(listIcon)) / 2, listY + 7, iconCol);

        // 2. Settings button [ ⚙ ]
        int setX = listX + listW + 6;
        int setY = pillY;
        int setW = 24;
        int setH = 24;
        boolean hoverSet = mouseX >= setX && mouseX <= setX + setW && mouseY >= setY && mouseY <= setY + setH;
        int setBg = (settingsOpen || hoverSet) ? 0xE6202028 : 0xD0141418;
        int setBorder = (settingsOpen || hoverSet) ? 0xFF38B6FF : 0x25FFFFFF;
        RenderUtil.drawRoundedRect(setX, setY, setW, setH, 6.0f, setBg);
        RenderUtil.drawRoundedOutline(setX, setY, setW, setH, 6.0f, 1.0f, setBorder);
        String setIcon = "⚙";
        int setCol = (settingsOpen || hoverSet) ? 0xFF38B6FF : 0xFFB0B0B8;
        fr.drawString(setIcon, setX + (setW - fr.getStringWidth(setIcon)) / 2, setY + 7, setCol);

        // Render toast if active
        if (System.currentTimeMillis() < toastEndTime && toastText != null) {
            long remaining = toastEndTime - System.currentTimeMillis();
            float alpha = 1.0f;
            if (remaining < 500) {
                alpha = remaining / 500.0f;
            } else if (remaining > 2200) {
                alpha = (2500 - remaining) / 300.0f;
            }
            int alphaInt = (int) (alpha * 240.0f);
            if (alphaInt > 10) {
                int toastW = fr.getStringWidth(toastText) + 24;
                int toastH = 22;
                int toastX = screenWidth / 2 - toastW / 2;
                int toastY = screenHeight - 45;
                int bg = (alphaInt << 24) | 0x141418;
                int border = ((int) (alpha * 120.0f) << 24) | 0x38B6FF;
                int textCol = ((int) (alpha * 255.0f) << 24) | 0xFFFFFF;

                RenderUtil.drawRoundedRect(toastX, toastY, toastW, toastH, 6.0f, bg);
                RenderUtil.drawRoundedOutline(toastX, toastY, toastW, toastH, 6.0f, 1.0f, border);
                fr.drawString(toastText, toastX + 12, toastY + 7, textCol);
            }
        }

        // Render Settings Dialog if open
        if (settingsOpen) {
            renderSettingsDialog(mouseX, mouseY, screenWidth, screenHeight);
        } else if (browserOpen) {
            renderBrowserDialog(mouseX, mouseY, screenWidth, screenHeight);
        }
    }

    private void renderSettingsDialog(int mouseX, int mouseY, int screenWidth, int screenHeight) {
        FontRenderer fr = mc.fontRendererObj;
        if (fr == null) return;

        // Dark modal overlay
        RenderUtil.drawRect(0, 0, screenWidth, screenHeight, 0x90000000);

        int dw = 330;
        int dh = 275;
        int dx = screenWidth / 2 - dw / 2;
        int dy = screenHeight / 2 - dh / 2;

        // Dialog background card
        RenderUtil.drawRoundedRect(dx, dy, dw, dh, 8.0f, 0xFA18181C);
        RenderUtil.drawRoundedOutline(dx, dy, dw, dh, 8.0f, 1.0f, 0x4038B6FF);

        // Header Title
        fr.drawString("着色器渲染设置", dx + 14, dy + 12, 0xFFFFFFFF);
        fr.drawString("性能与动画调优", dx + 14, dy + 24, 0xFF888890);

        // Close button [ ✕ ]
        int closeX = dx + dw - 24;
        int closeY = dy + 10;
        boolean hoverClose = mouseX >= closeX && mouseX <= closeX + 16 && mouseY >= closeY && mouseY <= closeY + 16;
        fr.drawString("✕", closeX, closeY + 2, hoverClose ? 0xFFFF5555 : 0xFF888890);

        int sliderW = dw - 32;

        // Setting 1: 分辨率 (0 - 200, 默认 100)
        int s1Y = dy + 45;
        String resText = (int) resolution + "%";
        if (resolution <= 0.0) resText = "0% (10% 最低)";
        fr.drawString("分辨率 (Resolution)", dx + 16, s1Y, 0xFFDDDDDD);
        fr.drawString(resText, dx + dw - 16 - fr.getStringWidth(resText), s1Y, 0xFF38B6FF);
        fr.drawString("降低画面渲染分辨率，低配显卡推荐 50%-75%", dx + 16, s1Y + 11, 0xFF707078);
        drawSliderTrack(dx + 16, s1Y + 23, sliderW, (float) (resolution / 200.0), mouseX, mouseY);

        // Setting 2: 帧率限制 (0 - 600, 0 为垂直同步)
        int s2Y = dy + 95;
        String fpsText = (int) fpsLimit == 0 ? "0 (垂直同步)" : ((int) fpsLimit + " FPS");
        fr.drawString("帧率限制 (FPS Limit)", dx + 16, s2Y, 0xFFDDDDDD);
        fr.drawString(fpsText, dx + dw - 16 - fr.getStringWidth(fpsText), s2Y, 0xFF38B6FF);
        fr.drawString("0 为垂直同步，限制帧率可显著降低 GPU 负载", dx + 16, s2Y + 11, 0xFF707078);
        drawSliderTrack(dx + 16, s2Y + 23, sliderW, (float) (fpsLimit / 600.0), mouseX, mouseY);

        // Setting 3: 动画倍速 (0 - 10, 默认为 1)
        int s3Y = dy + 145;
        String spdText = String.format("%.1fx", speed) + (speed == 0.0 ? " (已暂停)" : "");
        fr.drawString("动画倍速 (Animation Speed)", dx + 16, s3Y, 0xFFDDDDDD);
        fr.drawString(spdText, dx + dw - 16 - fr.getStringWidth(spdText), s3Y, 0xFF38B6FF);
        fr.drawString("调整着色器动态演进速率，0 为完全静止", dx + 16, s3Y + 11, 0xFF707078);
        drawSliderTrack(dx + 16, s3Y + 23, sliderW, (float) (speed / 10.0), mouseX, mouseY);

        // Divider
        RenderUtil.drawLine(dx + 16, dy + 215, dx + dw - 16, dy + 215, 1.0f, 0x20FFFFFF);

        // Button 1: Reset Defaults
        int btnW = 140;
        int btnH = 22;
        int rBtnX = dx + 16;
        int rBtnY = dy + 230;
        boolean hoverReset = mouseX >= rBtnX && mouseX <= rBtnX + btnW && mouseY >= rBtnY && mouseY <= rBtnY + btnH;
        int rBg = hoverReset ? 0x4038B6FF : 0x20FFFFFF;
        RenderUtil.drawRoundedRect(rBtnX, rBtnY, btnW, btnH, 4.0f, rBg);
        RenderUtil.drawRoundedOutline(rBtnX, rBtnY, btnW, btnH, 4.0f, 1.0f, hoverReset ? 0xFF38B6FF : 0x30FFFFFF);
        String resetStr = "重置为默认值";
        fr.drawString(resetStr, rBtnX + (btnW - fr.getStringWidth(resetStr)) / 2, rBtnY + 7, hoverReset ? 0xFF38B6FF : 0xFFCCCCCC);

        // Button 2: Done
        int dBtnX = dx + dw - 16 - btnW;
        int dBtnY = dy + 230;
        boolean hoverDone = mouseX >= dBtnX && mouseX <= dBtnX + btnW && mouseY >= dBtnY && mouseY <= dBtnY + btnH;
        int dBg = hoverDone ? 0xFF4AC4FF : 0xFF38B6FF;
        RenderUtil.drawRoundedRect(dBtnX, dBtnY, btnW, btnH, 4.0f, dBg);
        String doneStr = "完成 (Done)";
        fr.drawString(doneStr, dBtnX + (btnW - fr.getStringWidth(doneStr)) / 2, dBtnY + 7, 0xFF101014);
    }

    private void drawSliderTrack(int trackX, int trackY, int trackW, float ratio, int mouseX, int mouseY) {
        ratio = Math.max(0.0f, Math.min(1.0f, ratio));
        int trackH = 6;

        // Background track
        RenderUtil.drawRoundedRect(trackX, trackY, trackW, trackH, 3.0f, 0x40101014);
        RenderUtil.drawRoundedOutline(trackX, trackY, trackW, trackH, 3.0f, 1.0f, 0x20FFFFFF);

        // Filled track
        int filledW = (int) (trackW * ratio);
        if (filledW > 0) {
            RenderUtil.drawRoundedRect(trackX, trackY, filledW, trackH, 3.0f, 0xFF38B6FF);
        }

        // Knob
        int knobSize = 12;
        int knobX = trackX + filledW - knobSize / 2;
        int knobY = trackY + trackH / 2 - knobSize / 2;
        boolean hoverKnob = mouseX >= knobX && mouseX <= knobX + knobSize && mouseY >= knobY && mouseY <= knobY + knobSize;

        RenderUtil.drawRoundedRect(knobX, knobY, knobSize, knobSize, knobSize / 2.0f, hoverKnob ? 0xFFFFFFFF : 0xFFEEEEEE);
        RenderUtil.drawRoundedOutline(knobX, knobY, knobSize, knobSize, knobSize / 2.0f, 1.5f, 0xFF38B6FF);
    }

    private void updateSliderDrag(int mouseX) {
        int dw = 330;
        int screenWidth = mc.currentScreen != null ? mc.currentScreen.width : 400;
        int dx = screenWidth / 2 - dw / 2;
        int sliderX = dx + 16;
        int sliderW = dw - 32;

        float ratio = Math.max(0.0f, Math.min(1.0f, (float) (mouseX - sliderX) / (float) sliderW));

        if (draggingSlider == 1) {
            // Resolution: 0 to 200, step 5
            double val = Math.round((ratio * 200.0) / 5.0) * 5.0;
            setResolution(val);
        } else if (draggingSlider == 2) {
            // FPS Limit: 0 to 600, step 10, snap to 0 if < 5
            double val = Math.round((ratio * 600.0) / 10.0) * 10.0;
            if (val <= 10.0 && ratio < 0.02f) val = 0.0;
            setFpsLimit(val);
        } else if (draggingSlider == 3) {
            // Speed: 0.0 to 10.0, step 0.1
            double val = Math.round((ratio * 10.0) * 10.0) / 10.0;
            setSpeed(val);
        }
    }

    private void renderBrowserDialog(int mouseX, int mouseY, int screenWidth, int screenHeight) {
        FontRenderer fr = mc.fontRendererObj;
        if (fr == null) return;

        // Dark modal overlay
        RenderUtil.drawRect(0, 0, screenWidth, screenHeight, 0x90000000);

        int dw = 320;
        int dh = 300;
        int dx = screenWidth / 2 - dw / 2;
        int dy = screenHeight / 2 - dh / 2;

        // Dialog background card
        RenderUtil.drawRoundedRect(dx, dy, dw, dh, 8.0f, 0xFA18181C);
        RenderUtil.drawRoundedOutline(dx, dy, dw, dh, 8.0f, 1.0f, 0x4038B6FF);

        // Header Title
        fr.drawString("Background Shaders", dx + 14, dy + 12, 0xFFFFFFFF);
        String countText = shaderList.size() + " Shaders";
        fr.drawString(countText, dx + dw - 14 - fr.getStringWidth(countText) - 18, dy + 13, 0xFF888890);

        // Close button [ ✕ ]
        int closeX = dx + dw - 24;
        int closeY = dy + 10;
        boolean hoverClose = mouseX >= closeX && mouseX <= closeX + 16 && mouseY >= closeY && mouseY <= closeY + 16;
        fr.drawString("✕", closeX, closeY + 2, hoverClose ? 0xFFFF5555 : 0xFF888890);

        // Search Input Box
        int sx = dx + 12;
        int sy = dy + 32;
        int sw = dw - 24;
        int sh = 22;
        RenderUtil.drawRoundedRect(sx, sy, sw, sh, 4.0f, 0xFF101014);
        RenderUtil.drawRoundedOutline(sx, sy, sw, sh, 4.0f, 1.0f, 0x3038B6FF);

        if (searchQuery.isEmpty()) {
            fr.drawString("Search shaders...", sx + 8, sy + 7, 0xFF666670);
        } else {
            String cursor = (System.currentTimeMillis() % 1000 < 500) ? "_" : "";
            fr.drawString(searchQuery + cursor, sx + 8, sy + 7, 0xFFEEEEEE);
        }

        // List Area
        int lx = dx + 12;
        int ly = dy + 60;
        int lw = dw - 24;
        int lh = dh - 72;

        List<String> filtered = getFilteredShaders();
        int itemH = 22;
        int visibleCount = lh / itemH;

        if (scrollOffset > Math.max(0, filtered.size() - visibleCount)) {
            scrollOffset = Math.max(0, filtered.size() - visibleCount);
        }

        int startIdx = scrollOffset;
        int endIdx = Math.min(filtered.size(), startIdx + visibleCount);

        for (int i = startIdx; i < endIdx; i++) {
            String name = filtered.get(i);
            int iy = ly + (i - startIdx) * itemH;
            boolean isCur = name.equalsIgnoreCase(currentShader);
            boolean hoverItem = mouseX >= lx && mouseX <= lx + lw && mouseY >= iy && mouseY <= iy + itemH - 2;

            int itemBg = isCur ? 0x3538B6FF : (hoverItem ? 0x20FFFFFF : 0x00000000);
            if (itemBg != 0) {
                RenderUtil.drawRoundedRect(lx, iy, lw, itemH - 2, 4.0f, itemBg);
            }
            if (isCur) {
                RenderUtil.drawRoundedRect(lx, iy + 2, 3, itemH - 6, 1.5f, 0xFF38B6FF);
            }

            int itemTextCol = isCur ? 0xFF38B6FF : (hoverItem ? 0xFFFFFFFF : 0xFFD0D0D8);
            String itemDisp = formatShaderName(name);
            fr.drawString(itemDisp, lx + 12, iy + 6, itemTextCol);

            if (isCur) {
                String activeTag = "Active";
                fr.drawString(activeTag, lx + lw - fr.getStringWidth(activeTag) - 8, iy + 6, 0xFF38B6FF);
            }
        }

        // Scrollbar
        if (filtered.size() > visibleCount) {
            float total = (float) filtered.size();
            float sbH = Math.max(16.0f, ((float) visibleCount / total) * (float) lh);
            float sbY = (float) ly + ((float) scrollOffset / (total - visibleCount)) * ((float) lh - sbH);
            RenderUtil.drawRoundedRect(dx + dw - 7, sbY, 3.0f, sbH, 1.5f, 0x6038B6FF);
        }
    }

    private List<String> getFilteredShaders() {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return shaderList;
        }
        String q = searchQuery.trim().toLowerCase();
        List<String> result = new ArrayList<>();
        for (String s : shaderList) {
            if (s.toLowerCase().contains(q)) {
                result.add(s);
            }
        }
        return result;
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) return false;

        // 1. Settings Dialog Clicks
        if (settingsOpen) {
            int dw = 330;
            int dh = 275;
            int screenWidth = mc.currentScreen != null ? mc.currentScreen.width : 400;
            int screenHeight = mc.currentScreen != null ? mc.currentScreen.height : 300;
            int dx = screenWidth / 2 - dw / 2;
            int dy = screenHeight / 2 - dh / 2;

            // Close button [ ✕ ]
            int closeX = dx + dw - 24;
            int closeY = dy + 10;
            if (mouseX >= closeX && mouseX <= closeX + 16 && mouseY >= closeY && mouseY <= closeY + 16) {
                settingsOpen = false;
                playClickSound();
                return true;
            }

            // Click outside dialog closes it
            if (mouseX < dx || mouseX > dx + dw || mouseY < dy || mouseY > dy + dh) {
                settingsOpen = false;
                playClickSound();
                return true;
            }

            int sliderW = dw - 32;

            // Check slider 1: Resolution (dy + 68)
            int s1Y = dy + 45 + 23;
            if (mouseX >= dx + 12 && mouseX <= dx + 16 + sliderW + 4 && mouseY >= s1Y - 6 && mouseY <= s1Y + 12) {
                draggingSlider = 1;
                updateSliderDrag(mouseX);
                return true;
            }

            // Check slider 2: FPS (dy + 118)
            int s2Y = dy + 95 + 23;
            if (mouseX >= dx + 12 && mouseX <= dx + 16 + sliderW + 4 && mouseY >= s2Y - 6 && mouseY <= s2Y + 12) {
                draggingSlider = 2;
                updateSliderDrag(mouseX);
                return true;
            }

            // Check slider 3: Speed (dy + 168)
            int s3Y = dy + 145 + 23;
            if (mouseX >= dx + 12 && mouseX <= dx + 16 + sliderW + 4 && mouseY >= s3Y - 6 && mouseY <= s3Y + 12) {
                draggingSlider = 3;
                updateSliderDrag(mouseX);
                return true;
            }

            // Button 1: Reset Defaults
            int btnW = 140;
            int btnH = 22;
            int rBtnX = dx + 16;
            int rBtnY = dy + 230;
            if (mouseX >= rBtnX && mouseX <= rBtnX + btnW && mouseY >= rBtnY && mouseY <= rBtnY + btnH) {
                setResolution(100.0);
                setFpsLimit(0.0);
                setSpeed(1.0);
                playClickSound();
                showToast("已恢复默认着色器设置");
                return true;
            }

            // Button 2: Done
            int dBtnX = dx + dw - 16 - btnW;
            int dBtnY = dy + 230;
            if (mouseX >= dBtnX && mouseX <= dBtnX + btnW && mouseY >= dBtnY && mouseY <= dBtnY + btnH) {
                settingsOpen = false;
                playClickSound();
                return true;
            }

            return true;
        }

        // 2. Browser Dialog Clicks
        if (browserOpen) {
            int dw = 320;
            int dh = 300;
            int screenWidth = mc.currentScreen != null ? mc.currentScreen.width : 400;
            int screenHeight = mc.currentScreen != null ? mc.currentScreen.height : 300;
            int dx = screenWidth / 2 - dw / 2;
            int dy = screenHeight / 2 - dh / 2;

            // Close button [ ✕ ]
            int closeX = dx + dw - 24;
            int closeY = dy + 10;
            if (mouseX >= closeX && mouseX <= closeX + 16 && mouseY >= closeY && mouseY <= closeY + 16) {
                browserOpen = false;
                playClickSound();
                return true;
            }

            // Click outside dialog closes it
            if (mouseX < dx || mouseX > dx + dw || mouseY < dy || mouseY > dy + dh) {
                browserOpen = false;
                playClickSound();
                return true;
            }

            // Click on list item
            int lx = dx + 12;
            int ly = dy + 60;
            int lw = dw - 24;
            int lh = dh - 72;
            int itemH = 22;
            List<String> filtered = getFilteredShaders();

            if (mouseX >= lx && mouseX <= lx + lw && mouseY >= ly && mouseY <= ly + lh) {
                int clickedIndex = scrollOffset + (mouseY - ly) / itemH;
                if (clickedIndex >= 0 && clickedIndex < filtered.size()) {
                    String selected = filtered.get(clickedIndex);
                    setShader(selected);
                    return true;
                }
            }

            return true;
        }

        int pillX = 12;
        int pillY = 12;
        int pillW = 210;
        int pillH = 24;

        // Prev chevron button
        int prevX = pillX + 3;
        int prevY = pillY + 3;
        int prevW = 18;
        int prevH = 18;
        if (mouseX >= prevX && mouseX <= prevX + prevW && mouseY >= prevY && mouseY <= prevY + prevH) {
            prevShader();
            return true;
        }

        // Next chevron button
        int nextX = pillX + pillW - 21;
        int nextY = pillY + 3;
        int nextW = 18;
        int nextH = 18;
        if (mouseX >= nextX && mouseX <= nextX + nextW && mouseY >= nextY && mouseY <= nextY + nextH) {
            nextShader();
            return true;
        }

        // Browse list button [ ≡ ]
        int listX = pillX + pillW + 6;
        int listY = pillY;
        int listW = 24;
        int listH = 24;
        if (mouseX >= listX && mouseX <= listX + listW && mouseY >= listY && mouseY <= listY + listH) {
            browserOpen = true;
            settingsOpen = false;
            searchQuery = "";
            scrollOffset = 0;
            playClickSound();
            return true;
        }

        // Settings button [ ⚙ ]
        int setX = listX + listW + 6;
        int setY = pillY;
        int setW = 24;
        int setH = 24;
        if (mouseX >= setX && mouseX <= setX + setW && mouseY >= setY && mouseY <= setY + setH) {
            settingsOpen = true;
            browserOpen = false;
            playClickSound();
            return true;
        }

        // Middle pill click -> cycles next
        if (mouseX >= pillX && mouseX <= pillX + pillW && mouseY >= pillY && mouseY <= pillY + pillH) {
            nextShader();
            return true;
        }

        return false;
    }

    public boolean handleMouseWheel(int dWheel, int mouseX, int mouseY) {
        if (dWheel == 0) return false;

        if (settingsOpen) {
            return true; // Consume wheel inside settings dialog
        }

        if (browserOpen) {
            int dw = 320;
            int dh = 300;
            int screenWidth = mc.currentScreen != null ? mc.currentScreen.width : 400;
            int screenHeight = mc.currentScreen != null ? mc.currentScreen.height : 300;
            int dx = screenWidth / 2 - dw / 2;
            int dy = screenHeight / 2 - dh / 2;

            if (mouseX >= dx && mouseX <= dx + dw && mouseY >= dy && mouseY <= dy + dh) {
                int scrollDir = (dWheel > 0) ? -2 : 2;
                List<String> filtered = getFilteredShaders();
                int itemH = 22;
                int visibleCount = (dh - 72) / itemH;
                int maxScroll = Math.max(0, filtered.size() - visibleCount);
                scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset + scrollDir));
                return true;
            }
            return true;
        }

        int pillX = 12;
        int pillY = 12;
        int pillW = 270;
        int pillH = 24;

        if (mouseX >= pillX && mouseX <= pillX + pillW && mouseY >= pillY && mouseY <= pillY + pillH) {
            if (dWheel > 0) {
                prevShader();
            } else {
                nextShader();
            }
            return true;
        }

        return false;
    }

    public boolean keyTyped(char typedChar, int keyCode) {
        if (settingsOpen) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                settingsOpen = false;
                playClickSound();
                return true;
            }
            return true; // Consume keys in settings modal
        }

        if (browserOpen) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                browserOpen = false;
                playClickSound();
                return true;
            }
            if (keyCode == Keyboard.KEY_BACK) {
                if (!searchQuery.isEmpty()) {
                    searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                    scrollOffset = 0;
                }
                return true;
            }
            if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                searchQuery += typedChar;
                scrollOffset = 0;
                return true;
            }
            return true;
        }

        if (keyCode == Keyboard.KEY_LEFT) {
            prevShader();
            return true;
        } else if (keyCode == Keyboard.KEY_RIGHT) {
            nextShader();
            return true;
        }

        return false;
    }

    public static String formatShaderName(String name) {
        if (name == null || name.isEmpty()) return "";
        if ("Vanilla".equalsIgnoreCase(name)) return "Vanilla";
        String formatted = name.replace('_', ' ');
        if (formatted.length() > 0 && Character.isLowerCase(formatted.charAt(0))) {
            formatted = Character.toUpperCase(formatted.charAt(0)) + formatted.substring(1);
        }
        return formatted;
    }
}
