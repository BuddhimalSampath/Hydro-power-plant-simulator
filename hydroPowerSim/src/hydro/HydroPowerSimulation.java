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
    private Texture gatewayTex;
    private Texture waterFlowTex;

    private float waterFlowOffset = 0.0f;

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
            gatewayTex = TextureIO.newTexture(getClass().getResourceAsStream("/images/gateway.png"), false, "png");
            waterFlowTex = TextureIO.newTexture(getClass().getResourceAsStream("/images/waterflow.png"), false, "png");
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
        waterFlowOffset += gatewayOpenLevel * 0.01f;

        drawBackground(gl);
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

    private void drawWaterFlow(GL2 gl) {
        waterFlowTex.enable(gl);
        waterFlowTex.bind(gl);

        float offset = waterFlowOffset % 1.0f;
        TextureCoords coords = waterFlowTex.getImageTexCoords();

        gl.glBegin(GL2.GL_QUADS);
        gl.glTexCoord2f(coords.left(), coords.bottom() + offset);
        gl.glVertex2f(100, 100);
        gl.glTexCoord2f(coords.right(), coords.bottom() + offset);
        gl.glVertex2f(700, 100);
        gl.glTexCoord2f(coords.right(), coords.top() + offset);
        gl.glVertex2f(700, 200);
        gl.glTexCoord2f(coords.left(), coords.top() + offset);
        gl.glVertex2f(100, 200);
        gl.glEnd();

        waterFlowTex.disable(gl);
    }

    private void drawTurbine(GL2 gl) {
        gl.glPushMatrix();
        gl.glTranslatef(300, 250, 0);
        gl.glRotatef(turbineAngle, 0, 0, 1);

        turbineTex.enable(gl);
        turbineTex.bind(gl);

        float size = 60;
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
        float openY = 200 + gatewayOpenLevel * 100;

        gatewayTex.enable(gl);
        gatewayTex.bind(gl);

        gl.glPushMatrix();
        gl.glTranslatef(100, openY - 50, 0);
        float width = 40;
        float height = 100 * gatewayOpenLevel;

        gl.glBegin(GL2.GL_QUADS);
        gl.glTexCoord2f(0, 0); gl.glVertex2f(0, 0);
        gl.glTexCoord2f(1, 0); gl.glVertex2f(width, 0);
        gl.glTexCoord2f(1, 1); gl.glVertex2f(width, height);
        gl.glTexCoord2f(0, 1); gl.glVertex2f(0, height);
        gl.glEnd();

        gatewayTex.disable(gl);
        gl.glPopMatrix();
    }
}
