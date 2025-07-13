package hydro;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.TextureCoords;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class HydroPowerSimulation implements GLEventListener {

    private float gatewayOpenLevel = 0.5f;
    private float turbineSpeed = 0;
    private float turbineAngle = 0;
    private JLabel turbineLabel, powerLabel, gatewayLabel;

    private Texture backgroundTex;
    private Texture turbineTex;
    private Texture waterFlowTex;
    private Texture gatewayTex;

    private float waterFlowOffset = 0.0f;
    private float waterFlowSpeed = 0.0f;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HydroPowerSimulation().start());
    }

    private void start() {
        // JOGL Setup
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities caps = new GLCapabilities(profile);
        GLCanvas canvas = new GLCanvas(caps);
        canvas.addGLEventListener(this);
        FPSAnimator animator = new FPSAnimator(canvas, 60);
        animator.start();

        // UI Panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(4, 1, 10, 10));

        JLabel sliderLabel = new JLabel("Gateway Opening (%)", JLabel.CENTER);
        JSlider slider = new JSlider(0, 100, (int) (gatewayOpenLevel * 100));
        turbineLabel = new JLabel("Turbine Speed: 0 rpm", JLabel.CENTER);
        powerLabel = new JLabel("Power Output: 0 W", JLabel.CENTER);
        gatewayLabel = new JLabel("Gateway Width: 0.0 m", JLabel.CENTER);

        slider.addChangeListener(e -> {
            gatewayOpenLevel = slider.getValue() / 100.0f;
            updateTurbineSpeed();
            updateLabels();
        });

        controlPanel.add(sliderLabel);
        controlPanel.add(slider);
        controlPanel.add(turbineLabel);
        controlPanel.add(powerLabel);
        controlPanel.add(gatewayLabel);

        // Main Frame
        JFrame frame = new JFrame("Hydropower Plant Simulation");
        frame.setLayout(new BorderLayout());
        frame.add(canvas, BorderLayout.CENTER);
        frame.add(controlPanel, BorderLayout.EAST);
        frame.setSize(1000, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void updateLabels() {
        turbineLabel.setText(String.format("Turbine Speed: %.1f rpm", turbineSpeed * 60));
        powerLabel.setText(String.format("Power Output: %.1f W", turbineSpeed * 120));
        gatewayLabel.setText(String.format("Gateway Width: %.1f m", gatewayOpenLevel * 5));
    }

    private void updateTurbineSpeed() {
        turbineSpeed = gatewayOpenLevel * 5;
        waterFlowSpeed = gatewayOpenLevel * 0.02f; // Water flow speed depends on gateway opening
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClearColor(0.7f, 0.85f, 1f, 1.0f);
        gl.glEnable(GL2.GL_TEXTURE_2D);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        try {
            backgroundTex = TextureIO.newTexture(getClass().getResourceAsStream("/images/background.jpg"), false, "jpg");
            turbineTex = TextureIO.newTexture(getClass().getResourceAsStream("/images/turbine.png"), false, "png");
            waterFlowTex = TextureIO.newTexture(getClass().getResourceAsStream("/images/waterflow.png"), false, "png");
            gatewayTex = TextureIO.newTexture(getClass().getResourceAsStream("/images/gateway.png"), false, "png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {}

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, w, h);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(0, w, 0, h, -1, 1);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);

        updateTurbineSpeed();
        turbineAngle += turbineSpeed;
        waterFlowOffset += waterFlowSpeed;

        drawBackground(gl);
        drawRectangles(gl);
        drawWaterFlow(gl);
        drawTurbine(gl);
        drawGateway(gl);  
    }

    private void drawBackground(GL2 gl) {
        backgroundTex.enable(gl);
        backgroundTex.bind(gl);

        gl.glBegin(GL2.GL_QUADS);
        gl.glTexCoord2f(0, 0); gl.glVertex2f(0, 0);
        gl.glTexCoord2f(1, 0); gl.glVertex2f(800, 0);
        gl.glTexCoord2f(1, 1); gl.glVertex2f(800, 600);
        gl.glTexCoord2f(0, 1); gl.glVertex2f(0, 600);
        gl.glEnd();

        backgroundTex.disable(gl);
    }

    private void drawRectangles(GL2 gl) {
        gl.glDisable(GL2.GL_TEXTURE_2D);
        
        // upper door
        gl.glColor3f(0.6f, 0.2f, 0.2f); // Dark red color
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex2f(100, 325);   // Bottom left
        gl.glVertex2f(150, 325);  // Bottom right
        gl.glVertex2f(140, 400);  // Top right
        gl.glVertex2f(110, 400);   // Top left
        gl.glEnd();

        // middle in door
        gl.glColor3f(0.6f, 0.2f, 0.2f); // Dark red color
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex2f(100, 230);  // Bottom left
        gl.glVertex2f(150, 230);  // Bottom right
        gl.glVertex2f(150, 325);  // Top right
        gl.glVertex2f(100, 325);  // Top left
        gl.glEnd();

        // ground
        gl.glColor3f(0.6f, 0.2f, 0.2f); // Dark red color
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex2f(0, 200);   // Bottom left
        gl.glVertex2f(750, 200);   // Bottom right
        gl.glVertex2f(750, 230);  // Top right
        gl.glVertex2f(0, 230);  // Top left
        gl.glEnd();
        
        // up boundry
        gl.glColor3f(0.6f, 0.2f, 0.2f); // Dark red color
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex2f(150, 300);   // Bottom left
        gl.glVertex2f(750, 300);   // Bottom right
        gl.glVertex2f(750, 325);  // Top right
        gl.glVertex2f(150, 325);  // Top left
        gl.glEnd();

        gl.glColor3f(1.0f, 1.0f, 1.0f); // Reset color to white
        gl.glEnable(GL2.GL_TEXTURE_2D);
    }

    private void drawWaterFlow(GL2 gl) {
        if (gatewayOpenLevel > 0.01f) { // Only draw water flow if gateway is open
            waterFlowTex.enable(gl);
            waterFlowTex.bind(gl);

            float offset = waterFlowOffset % 1.0f;
            TextureCoords coords = waterFlowTex.getImageTexCoords();

            // Simple horizontal water flow from left to right (similar to original)
            gl.glBegin(GL2.GL_QUADS);
            gl.glTexCoord2f(coords.left() - offset, coords.bottom());
            gl.glVertex2f(120, 230);    //bottom left
            gl.glTexCoord2f(coords.right() - offset, coords.bottom());
            gl.glVertex2f(750, 230);    //bottom right
            gl.glTexCoord2f(coords.right() - offset, coords.top());
            gl.glVertex2f(750, 300);       //top right
            gl.glTexCoord2f(coords.left() - offset, coords.top());
            gl.glVertex2f(120, 300);        //top left
            gl.glEnd();

            waterFlowTex.disable(gl);
        }
    }

    private void drawTurbine(GL2 gl) {
        gl.glPushMatrix();
        gl.glTranslatef(450, 300, 0);
        gl.glRotatef(turbineAngle, 0, 0, 1);

        turbineTex.enable(gl);
        turbineTex.bind(gl);

        float size = 200;
        gl.glBegin(GL2.GL_QUADS);
        gl.glTexCoord2f(0, 0); gl.glVertex2f(-size, -size);
        gl.glTexCoord2f(1, 0); gl.glVertex2f(size, -size);
        gl.glTexCoord2f(1, 1); gl.glVertex2f(size, size);
        gl.glTexCoord2f(0, 1); gl.glVertex2f(-size, size);
        gl.glEnd();

        turbineTex.disable(gl);
        gl.glPopMatrix();
    }

    private void drawGateway(GL2 gl) {
        float maxHeight = 100f;
        float height = maxHeight * (1.0f - gatewayOpenLevel); // Decrease height when opening
        float yTop = 230f; // fixed top position
        float yBottom = yTop+100 - height;

        gatewayTex.enable(gl);
        gatewayTex.bind(gl);

        gl.glPushMatrix();
        gl.glTranslatef(100, yBottom, 0);

        float texTop = 1.0f;
        float texBottom = gatewayOpenLevel; // Texture shrinks from bottom upward

        gl.glBegin(GL2.GL_QUADS);
        gl.glTexCoord2f(0, texTop); gl.glVertex2f(0, height);
        gl.glTexCoord2f(1, texTop); gl.glVertex2f(40, height);
        gl.glTexCoord2f(1, texBottom); gl.glVertex2f(40, 0);
        gl.glTexCoord2f(0, texBottom); gl.glVertex2f(0, 0);
        gl.glEnd();

        gatewayTex.disable(gl);
        gl.glPopMatrix();
    }
}