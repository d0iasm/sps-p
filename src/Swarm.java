import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;


public class Swarm extends JPanel {
    private int w;
    private int h;

    private int scale = 10;
    private int l = 10;
    private double timeStep = 0.002;

    private int pNum;
    private int pType;
    private int pPartition;
    private int pSize = 1;
    private List<Particle> particles;

    private int count = 0;
    Parameter paramManager;
    JTextArea paramsText;
    double[][] params;

    public Swarm(int num, int w, int h) {
        this.pNum = num;
        this.w = w;
        this.h = h;
        this.pType = 2;
        this.pPartition = pNum / pType;

        this.paramManager = new Parameter(3);
        this.params = paramManager.getParams();
        showParams();

        this.particles = new ArrayList<>(num);
        for (int i = 1; i <= num; i++) {
            particles.add(new Particle(i));
        }
    }

    public Swarm(int num, int w, int h, int type) {
        this.pNum = num;
        this.w = w;
        this.h = h;
        this.pType = type;
        this.pPartition = pNum / pType;

        this.paramManager = new Parameter(3);
        this.params = paramManager.getParams();
        showParams();

        this.particles = new ArrayList<>(num);
        for (int i = 1; i <= num; i++) {
            particles.add(new Particle(i));
        }
    }

    private void showParams() {
        this.setLayout(null);
        this.add(paramManager.getTitle());
        this.paramsText = paramManager.getParamsText();
        this.add(paramsText);

        JButton updateButton = paramManager.getUpdateButton();
        updateButton.addActionListener(e -> {
            this.remove(paramsText);
            this.paramsText = paramManager.getUpdateParamsText();
            this.add(paramsText);
        });
        this.add(updateButton);

        JButton randomButton = paramManager.getRandomButton();
        randomButton.addActionListener(e -> {
            this.remove(paramsText);
            this.paramsText = paramManager.getRandomParamsText();
            this.add(paramsText);
        });
        this.add(randomButton);
    }

    private double getKParam(int i, int j) {
        return params[(i - 1) / pPartition][(j - 1) / pPartition];
    }

    private double diffX(Particle pi, Particle pj) {
        return pj.x - pi.x;
    }

    private double diffXClosest(Particle pi, Particle pj) {
        /*
         * Pi doesn't change its position and Pj changes its position.
         * Return the closest X difference between Pi and moved 9 types Pj.
         */
        double tmp;
        int d[] = {-1, 0, 1};
        double diffX = diffX(pi, pj);
        for (int i = 0; i < 3; i++) {
            tmp = pj.x + l * d[i] - pi.x;
            if (Math.abs(tmp) < Math.abs(diffX)) {
                diffX = tmp;
            }
        }
        return diffX;
    }

    private double diffYClosest(Particle pi, Particle pj) {
        /*
         * Pi doesn't change its position and Pj changes its position.
         * Return the closest Y difference between Pi and moved 9 types Pj.
         */
        double tmp;
        int d[] = {-1, 0, 1};
        double diffY = diffY(pi, pj);
        for (int i = 0; i < 3; i++) {
            tmp = pj.y + l * d[i] - pi.y;
            if (Math.abs(tmp) < Math.abs(diffY)) {
                diffY = tmp;
            }
        }
        return diffY;
    }

    private double diffY(Particle pi, Particle pj) {
        return pj.y - pi.y;
    }

    private double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    private double distance(Particle pi, Particle pj) {
        double x1 = pi.x;
        double y1 = pi.y;
        double x2 = pj.x;
        double y2 = pj.y;
        return distance(x1, y1, x2, y2);
    }

    private double distanceClosest(Particle pi, Particle pj) {
        /*
         * Pi doesn't change its position and Pj changes its position.
         * Return the closest distance between Pi and moved 9 types Pj.
         */
        double tmp;
        int d[] = {-1, 0, 1};
        double closest = distance(pi, pj);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                tmp = distance(pi.x, pi.y, l * d[i] + pj.x, l * d[j] + pj.y);
                if (tmp < closest) {
                    closest = tmp;
                }
            }
        }
        return closest;
    }

    private double calcRungeKutta(double x) {
        double k1 = x;
        double k2 = x + k1 * timeStep * 0.5;
        double k3 = x + k2 * timeStep * 0.5;
        double k4 = x + k3 * timeStep;
        return (k1 + 2 * k2 + 2 * k3 + k4) * (timeStep / 6.0);
    }

    public void run() {
        double sumX;
        double sumY;
        double dis;
        double paramK;
        double rungeSumX;
        double rungeSumY;

        double tmpX, tmpY;

        List<Double> newX = new ArrayList<>(pNum);
        List<Double> newY = new ArrayList<>(pNum);

        double[][] kSums = new double[][]{
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0},
        };

        for (Particle p1 : particles) {
            sumX = 0;
            sumY = 0;

            for (Particle p2 : particles) {
                if (p1 == p2) continue;

                // TODO: 事前に距離の計算をしておく
                // dis: |Rij|.
//                dis = distance(p1, p2);
                dis = distanceClosest(p1, p2);

                // paramK: kij.
                paramK = getKParam(p1.id, p2.id);

                // TODO: Bug? |Rij|^-1 and |Rij|^-2
//                sumX += (diffX(p1, p2) / dis) * (paramK * Math.pow(dis, -1.0) - Math.pow(dis, -2.0));
//                sumY += (diffY(p1, p2) / dis) * (paramK * Math.pow(dis, -1.0) - Math.pow(dis, -2.0));
//                tmpX = (diffX(p1, p2) / dis);
//                tmpY = (diffY(p1, p2) / dis);

                tmpX = (diffXClosest(p1, p2) / dis) * (paramK * Math.pow(dis, -0.8) - (1 / dis));
                tmpY = (diffYClosest(p1, p2) / dis) * (paramK * Math.pow(dis, -0.8) - (1 / dis));

//                tmpX = (diffX(p1, p2) / dis) * (paramK * Math.pow(dis, -0.8) - (1 / dis));
//                tmpY = (diffY(p1, p2) / dis) * (paramK * Math.pow(dis, -0.8) - (1 / dis));

//                kSums[(p1.id - 1) / pPartition][(p2.id - 1) / pPartition] = tmpX + tmpY;

                sumX += tmpX;
                sumY += tmpY;
            }

            rungeSumX = calcRungeKutta(sumX);
            rungeSumY = calcRungeKutta(sumY);

            newX.add(p1.x + rungeSumX);
            newY.add(p1.y + rungeSumY);
        }

        for (int i = 0; i < pNum; i++) {
            particles.get(i).x = newX.get(i);
            particles.get(i).y = newY.get(i);
        }

//        flipKParamHeider(kSums);
//        paramManager.changeKParamHeider(kSums);
        this.params = paramManager.getParams();
//        balanceKParamHeider(kSums);

        count++;
        if (count % 100 == 0) {
            repaint();

            if (count % 1000 == 0) {
                System.out.println(paramManager.getParamChangedCount());
                paramManager.setParamChangedCount(0);
            }
            if (count % 5000 == 0) {
                printSwarmParam();
                if (count == 50000) {
                    printSwarmParam();
                    System.exit(0);
                }
            }
        }
    }

    public void printSwarmParam() {
        System.out.println("Print current kParams---------------------------");
        System.out.println("count: " + count);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                System.out.print(params[i][j] + ", ");
            }
            System.out.println(" ");
        }
        System.out.println("Print current kParams---------------------------");
    }

    public void printSwarmParam(double[][] params) {
        System.out.println("Print current kParams---------------------------");
        System.out.println("count: " + count);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                System.out.print(params[i][j] + ", ");
            }
            System.out.println(" ");
        }
        System.out.println("Print current kParams---------------------------");
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < h; i += (l * scale)) {
            g2.drawLine(0, i, w, i);
            g2.drawLine(i, 0, i, h);
        }

        for (Particle p : particles) {
            if (p.id <= pPartition) {
                g2.setColor(Color.RED);
            } else if (pPartition < p.id && p.id <= pPartition * 2) {
                g2.setColor(Color.BLUE);
            } else {
                g2.setColor(Color.GREEN);
            }

            g2.fill(new Ellipse2D.Double(
                    p.x * scale + w / 2,
                    p.y * scale + h / 2,
                    pSize * scale, pSize * scale));
        }
    }
}
