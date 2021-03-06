package sps_p;

import sps_p.utils.LogPlot;
import sps_p.utils.Pair;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Parameters are defined by using A, B, P, and M which is written in Kano's thesis(Mathematical Analysis for Non-reciprocal-interaction-based Model of Collective Behavior, 2017).
 * Kij = {
 * Ka      (1 <= i <= 2/N, 1 <= j <= 2/N)
 * Kp + Km (1 <= i <= 2/N, 2/N < j <= N)
 * Kp - Km (2/N < j <= N, 1 <= j <= 2/N)
 * Ka      (2/N < j <= N, 2/N < j <= N)
 * }
 */
public class ParameterKabpm extends Parameter {
    // TODO: Replace to BigDecimal because "double" type is unstable.
    private static double kA = 0.8;
    private static double kB = 0.4;
    private static double kP = 0.6;
    private static double kM = -0.8;

    private JPanel textA;
    private JPanel textB;
    private JPanel textP;
    private JPanel textM;

    private LogPlot plot;

    /**
     * Initialize this class itself and UIs.
     *
     * @param num
     * @param type
     * @param swarm
     */
    ParameterKabpm(int num, int type, Swarm swarm) {
        super(num, type, swarm);
        initABPMLayout(); // Set all JPanels and a Button in this function.
        initLogPlot(); // Set LogPlot up.
    }

    /**
     * Calculate the position of the center of gravity.
     * rg = N^(-1) * Σ(N, i=1)ri
     *
     * @param particles The list of particles.
     * @return The position of the center of gravity.
     */
    Pair<Double> getGravity(List<Particle> particles) {
        double sumX = 0;
        double sumY = 0;
        for (Particle p : particles) {
            sumX += p.x;
            sumY += p.y;
        }
        return new Pair<>(sumX / pNum, sumY / pNum);
    }

    /**
     * X = (N^(-1) * Σ(N, i=1)|ri - rg|)^(-1)
     * rg = N^(-1) * Σ(N, i=1)ri
     * rg denotes the position of the center of gravity.
     * X converges to zero when at least one of the particles moves an infinite distance from the center of gravity.
     *
     * @param particles The list of particles.
     * @return The reciprocal of the average of distance from the gravity.
     */
    double getX(List<Particle> particles) {
        double sum = 0;
        Pair<Double> rg = getGravity(particles);
        for (Particle ri : particles) {
            sum += swarm.distance(ri.x, ri.y, rg.x, rg.y);
        }
        return Math.pow(sum / pNum, -1.0);
    }

    /**
     * V = N^(-1) * Σ(N, i=1)|ri(dot)-rg(dot)|
     * V converges to zero when the relative velocities of all particles with respect to the center of gravity converge to zero.
     *
     * @param timeEvolution The list of the time evolution for each ri.
     * @param curG          The position of the center of gravity in current step.
     * @param nextG         The position of the center of gravity in next step.
     * @return The average of relative speed with the gravity.
     */
    double getV(List<Pair<Double>> timeEvolution, Pair<Double> curG, Pair<Double> nextG) {
        Pair<Double> dotrg = new Pair<>(nextG.x - curG.x, nextG.y - curG.y);
        double sum = 0.0;
        for (Pair<Double> dotri : timeEvolution) {
            sum += swarm.distance(dotri.x, dotri.y, dotrg.x, dotrg.y);
        }
        return sum / pNum;
    }

    /**
     * X = (N^(-1) * Σ(N, i=1)|ri - rg|)^(-1), rg = N^(-1) * Σ(N, i=1)ri
     * V = N^(-1) * Σ(N, i=1)|ri(dot)-rg(dot)|
     * x = log10(10^3*X+1)
     * y = log10(10^3*V+1)
     *
     * @param x The reciprocal of the average of distance from the gravity.
     * @param v The average of relative speed with the gravity.
     */
    void addPoint(double x, double v) {
        plot.addPoint(Math.log10(1000 * x + 1), Math.log10(1000 * v + 1));
    }

    private JPanel createNewTextArea(String labelText, double val, int x, int y) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("OpenSans", Font.PLAIN, 16));
        label.setBounds(0, 0, 60, 30);

        JTextField field = new JTextField(String.valueOf(val));
        field.setFont(new Font("OpenSans", Font.PLAIN, 16));
        field.setBounds(60, 4, 40, 26);
        field.setHorizontalAlignment(JTextField.CENTER);

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(label);
        panel.add(field);
        panel.setBackground(Color.WHITE);
        panel.setBounds(x, y, 100, 30);
        return panel;
    }

    /**
     * Should be called once in this constructor. Initialize a LogPlot for X and V parameters.
     */
    private void initLogPlot() {
        plot = new LogPlot("log10(10^3*X+1)", "log10(10^3*V+1)");
        plot.setBounds(10, 490, 260, 260);
        swarm.add(plot);
    }

    /**
     * Should be called once in this constructor. Initialize all JPanel for ABPM parameters and set them up.
     */
    private void initABPMLayout() {
        textA = createNewTextArea("k_a = ", kA, 20, 30 * 1 - 15);
        textB = createNewTextArea("k_b = ", kB, 20, 30 * 2 - 15);
        textP = createNewTextArea("k_p = ", kP, 20, 30 * 3 - 15);
        textM = createNewTextArea("k_m = ", kM, 20, 30 * 4 - 15);
        swarm.add(textA);
        swarm.add(textB);
        swarm.add(textP);
        swarm.add(textM);

        JButton updateButton = createButton("Update", 20, 140, 100, 30);
        updateButton.addActionListener(e -> {
            double a = Double.parseDouble(((JTextField) textA.getComponent(1)).getText());
            double b = Double.parseDouble(((JTextField) textB.getComponent(1)).getText());
            double p = Double.parseDouble(((JTextField) textP.getComponent(1)).getText());
            double m = Double.parseDouble(((JTextField) textM.getComponent(1)).getText());
            double[][] newParams = {
                    {a, p + m},
                    {p - m, b}
            };
            super.setParams(newParams);
            super.updateParamsText();
            swarm.reset();
            this.reset();
        });
        swarm.add(updateButton);
    }

    /**
     * Return the hard-coded parameter K which is for 2 types of particles.
     * Kij = {
     * Ka      (1 <= i <= 2/N, 1 <= j <= 2/N)
     * Kp + Km (1 <= i <= 2/N, 2/N < j <= N)
     * Kp - Km (2/N < j <= N, 1 <= j <= 2/N)
     * Ka      (2/N < j <= N, 2/N < j <= N)
     * }
     *
     * @return Parameter K for 2 types of particles.
     */
    @Override
    double[][] init2x2() {
        return new double[][]{
                {kA, kP + kM},
                {kP - kM, kB}
        };
    }

    @Override
    double[][] init3x3() {
        return new double[0][];
    }

    /**
     * Reset log log plot.
     */
    @Override
    void reset() {
        plot.clearAll();
    }
}
