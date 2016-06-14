import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.print.Book;
import java.lang.IllegalArgumentException;
import java.util.HashMap;
import java.util.LinkedList;

public class Foo implements Serializable, Cloneable {

    private ArrayList<Color> rgb;

    public Foo() {
        this.rgb = new ArrayList<Color>(3);
        BarB.returnObject(new JDialog());
    }
    
    public Encoding doSomething() {
    	return Encoding.ALAW;
    }

    public AudioFormat colorized(Book b1, Book b2, int random) throws Exception, IllegalArgumentException {
        LinkedList<Point2D> points = new LinkedList<Point2D>();
        points.add(new Point(10, 20));
        points.add(new Point(20, 20));
        points.add(new Point(30, 20));

        if(random > 100) {
            Bar barInstance = new Bar();
            barInstance.returnField(new JLabel("I am a JLabel object"));

            if(random < 150) {
                for(int i = 0 ; i < 10 ; ++i) {
                    JButton button = new JButton("I am a JButton object");
                }

                barInstance = new Bar(new JTextArea());

                return new AudioFormat(Encoding.ALAW, (float)1.0, 8, 2, 1, (float)1.0, true, new HashMap<String, Object>());
            }
            else {
                throw new Exception("a test exception");
            }
        }
        else {
            throw new IllegalArgumentException("a test illegal format exception");
        }
    }
}