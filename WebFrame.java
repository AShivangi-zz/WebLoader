import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import javax.swing.table.DefaultTableModel;

/*
 * This class manages the overall flow of the program
 */

public class WebFrame extends JFrame {
    private final DefaultTableModel tablemodel;
    private final JTable table;
    private final JPanel panel;
    private final JButton single,concurrent,stop;
    private final JTextField field;
    private final JLabel running,completed,elapsed;
    private final JProgressBar progress;
    private int runningThreads;
    private Semaphore sem;
    private int completedWorks;
    private Thread t;
    private static String path;
    private ArrayList<WebWorker> workers;
   
   public WebFrame(String filename) {
       //setting panel
       panel = new JPanel();
       panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
       setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       
       //table model
       String[] columnNames = new String[] { "url", "status"};
       tablemodel = new DefaultTableModel(columnNames, 0);
       table = new JTable(tablemodel);
       
       //scroll for a long list of URLs
       JScrollPane scrollpane = new JScrollPane(table);
       scrollpane.setPreferredSize(new Dimension(600,300));
       panel.add(scrollpane);
       
       //reading file with URLs
       try {
           BufferedReader br = new BufferedReader(new FileReader(path));
           String line = br.readLine();
           while(true){
               if(line == null) break;
               tablemodel.addRow(new String[]{line, ""});
               line = br.readLine();
           } 
       } catch (FileNotFoundException e) {
           System.out.println("File not found");
       } catch (IOException e) {
           System.out.println("IO");
       }
       
       //buttons
       single = new JButton("Single Thread Fetch");
       single.addActionListener(new ActionListener(){
       @Override
       public void actionPerformed(ActionEvent e) {
           fetchButtonClicked();
           t = new Thread(new Launcher(1));
           t.start();
       }
       });
       
       concurrent = new JButton("Concurrent Fetch");
       concurrent.addActionListener(new ActionListener(){
                  @Override
       public void actionPerformed(ActionEvent e) {
           fetchButtonClicked();
           t = new Thread(new Launcher(Integer.parseInt(field.getText())));
           t.start();
       }
       });
       
       stop = new JButton("Stop");
       stop.addActionListener(new ActionListener(){
                  @Override
       public void actionPerformed(ActionEvent arg0) {
           if(t!=null)t.interrupt();
           t = null;
           statusReset();
           single.setEnabled(true);
           concurrent.setEnabled(true);
          
       }
       });
       stop.setEnabled(false);
       
       //no of threads
       field = new JTextField();
       field.setMaximumSize(new Dimension(50,JTextField.HEIGHT));
       
       //progress
       running = new JLabel("Running: 0");
       completed = new JLabel("Completed: 0");
       elapsed = new JLabel("Elapsed: 0");
       progress = new JProgressBar();
      
       panel.add(single);
       panel.add(concurrent);
       panel.add(field);
       panel.add(running);
       panel.add(completed);
       panel.add(elapsed);
       panel.add(progress);
       panel.add(stop);
      
       this.add(panel);
       this.pack();
       this.setVisible(true);
   }
  
   public void increaseThreads(){
       runningThreads++;
       updateRunningLabel();
   }
  
   public synchronized void decreaseThreads(){
       runningThreads--;
       updateRunningLabel();
       sem.release();
   }
  
   private void increaseCompledted(){
       completedWorks++;
       completed.setText("Completed: " + completedWorks);
   }
  
   private void updateRunningLabel(){
       running.setText("Running: " + runningThreads);
   }
  
   public void updateTable(int rowNum,String str){
       increaseCompledted();
       progress.setValue(completedWorks);
       tablemodel.setValueAt(str, rowNum, 1);
   }
   
   private void statusReset(){
       completedWorks = 0;
       progress.setValue(0);
       running.setText("Running: 0");
       completed.setText("Completed: 0");
       elapsed.setText("Elapsed: 0");
       progress.setValue(0);
   }
  
   private void fetchButtonClicked(){
       statusReset();
       stop.setEnabled(true);
       single.setEnabled(false);
       concurrent.setEnabled(false);
       progress.setMaximum(tablemodel.getRowCount());
   }
   
   
   private class Launcher implements Runnable{
       private int workerLimit;

       public Launcher(int workerLimit) {
           this.workerLimit = workerLimit;
       }

       @Override
       public void run() {
           long start = System.currentTimeMillis();
           runningThreads = 1;
           updateRunningLabel();
           sem = new Semaphore(workerLimit);
           workers = new ArrayList<>();
           for (int i = 0; i < tablemodel.getRowCount(); i++) {
               try {
                   sem.acquire();
               } catch (InterruptedException e) {
                   break;
               }
               WebWorker w = new WebWorker((String)tablemodel.getValueAt(i, 0), i, WebFrame.this);
               workers.add(w);
               w.start();
           }
           for (int i = 0; i < workers.size(); i++) {
               try {
                   workers.get(i).join();
               } catch(InterruptedException e) {
               }
           }
           
           runningThreads--;
           updateRunningLabel();
           long end = System.currentTimeMillis();
           elapsed.setText("Elapsed: " + (end-start));
           stop.setEnabled(false);
           single.setEnabled(true);
           concurrent.setEnabled(true);
       }
      
   }
   
      public static void main(String[] args) {
       try {
           UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
       } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
           e.printStackTrace();
       }
       path = args[0];
       WebFrame w = new WebFrame(args[0]);
   }
}
