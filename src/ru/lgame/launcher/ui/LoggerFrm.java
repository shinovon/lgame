package ru.lgame.launcher.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.BoundedRangeModel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

public class LoggerFrm extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2989281868090546630L;
	
	private JPanel contentPane;
	private JTextArea textArea;
	
	private boolean scrolled = true;

	protected Object scrollLock = new Object();

	/**
	 * Create the frame.
	 */
	public LoggerFrm() {
		setTitle("Лог");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		MouseWheelListener l = new MouseWheelListener() {
			BoundedRangeModel brm = scrollPane.getVerticalScrollBar().getModel();
			public void mouseWheelMoved(MouseWheelEvent e) {
				synchronized(scrollLock) {
					if(e.getPreciseWheelRotation() < 0) scrolled = false;
					else {
						if((brm.getValue() + brm.getExtent()) == brm.getMaximum()) scrolled = true;
					}
				}
			}
		};
		scrollPane.addMouseWheelListener(l);
		scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
			BoundedRangeModel brm = scrollPane.getVerticalScrollBar().getModel();
			public void adjustmentValueChanged(AdjustmentEvent e) {
				synchronized(scrollLock) {
					if (!brm.getValueIsAdjusting()) {
						if (scrolled) brm.setValue(brm.getMaximum());
					} else {
						scrolled = ((brm.getValue() + brm.getExtent()) == brm.getMaximum());
					}
				}
			}
		});
		contentPane.add(scrollPane, BorderLayout.CENTER);
		
		textArea = new JTextArea();
		textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		textArea.setText("LGame Launcher Logger ["+ DateFormat.getDateInstance().format(new Date()) + "]\n");
		textArea.setForeground(Color.WHITE);
		textArea.setBackground(Color.BLACK);
		scrollPane.setViewportView(textArea);
		contentPane.setPreferredSize(new Dimension(540, 320));
		pack();
	}
	
	public void append(String s) {
		textArea.append(s);
	}

}
