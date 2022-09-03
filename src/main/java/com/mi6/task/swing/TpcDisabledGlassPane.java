package com.mi6.task.swing;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;

public class TpcDisabledGlassPane extends JComponent implements KeyListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final static Border MESSAGE_BORDER = new EmptyBorder(10, 10, 10, 10);
    private JLabel message = new JLabel();

    public TpcDisabledGlassPane() {
        // Set glass pane properties

        setOpaque(false);
        Color background = new Color(255,255, 255, 180);
        setBackground(background);
        setLayout(new BorderLayout());

        Icon imgIcon = new ImageIcon(this.getClass().getResource("/common/loading/loading.gif"));
        message = new JLabel(imgIcon);
        message.setBorder(BorderFactory.createEmptyBorder(25,25,25,25));
        message.setForeground(Color.BLACK   );
        message.setHorizontalTextPosition(SwingConstants.CENTER);
        message.setVerticalTextPosition(SwingConstants.BOTTOM);
        add(message);

        // Disable Mouse, Key and Focus events for the glass pane
        addMouseListener(new MouseAdapter() {
        });
        addMouseMotionListener(new MouseMotionAdapter() {
        });

        addKeyListener(this);

        setFocusTraversalKeysEnabled(false);
    }

    /*
     * The component is transparent but we want to paint the background to give
     * it the disabled look.
     */
    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getSize().width, getSize().height);
    }

    /*
     * The background color of the message label will be the same as the
     * background of the glass pane without the alpha value
     */
    @Override
    public void setBackground(Color background) {
        super.setBackground(background);
    }

    //
    // Implement the KeyListener to consume events
    //
    public void keyPressed(KeyEvent e) {
        e.consume();
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
        e.consume();
    }

    /*
     * Make the glass pane visible and change the cursor to the wait cursor
     *
     * A message can be displayed and it will be centered on the frame.
     */
    public void activate(String text) {
        if (text != null && text.length() > 0) {
            message.setVisible(true);
            message.setText(text);
        } else {
            message.setVisible(false);
        }

        setVisible(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        requestFocusInWindow();
    }

    /*
     * Hide the glass pane and restore the cursor
     */
    public void deactivate() {
        setCursor(null);
        setVisible(false);
        message.setVisible(false);
    }

    // updates the message on screen
    public void setMessage(String text) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                message.setText(text);
            }
        });
    }
}
