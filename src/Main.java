import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Compiler System");
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JLabel codePathLabel = new JLabel("Code Path:");
        JTextField codePathTextField = new JTextField(20);
        JButton browseButton = new JButton("Browse");
        JButton compileButton = new JButton("Compile");


        JLabel outLabel = new JLabel("Output:");
        JTextArea outTextArea = new JTextArea(20, 22);

        JLabel symLabel = new JLabel("Symbols:");
        JTextArea symTextArea = new JTextArea(20, 22);

        JLabel errLabel = new JLabel("Errors:");
        JTextArea errTextArea = new JTextArea(20, 22);

        JLabel inLabel = new JLabel("Input:");
        JTextArea inTextArea = new JTextArea(20, 50);


        JScrollPane outScrollPane = new JScrollPane(outTextArea);
        JScrollPane symScrollPane = new JScrollPane(symTextArea);
        JScrollPane errScrollPane = new JScrollPane(errTextArea);
        JScrollPane inScrollPane = new JScrollPane(inTextArea);

        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                // 设置对话框只能选择文件
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

                int result = fileChooser.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    String path = selectedFile.getAbsolutePath();
                    codePathTextField.setText(path);
                }
            }
        });


        compileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String inPath = codePathTextField.getText();
                int index = inPath.lastIndexOf(".");
                String prefix = inPath.substring(0, index);

                Lexer lexer = new Lexer();

                // 清空文本区域
                outTextArea.setText("");
                symTextArea.setText("");
                errTextArea.setText("");
                inTextArea.setText("");
                try {
                    lexer.GetInput(inPath, prefix + ".out", prefix + ".sym", prefix + ".err");
                    if(lexer.error){
                        System.out.println("词法分析出错！");
                    }else{
                        GrammarAnalysis grammarAnalysis = new GrammarAnalysis();
                        grammarAnalysis.handleInput(lexer.symbolArrayList);
                        grammarAnalysis.fAnalysisInput();
                    }
                    codePathTextField.setText("");
                    System.out.println("Compilation process completed!");

                    String outFilePath = prefix + ".out";
                    String symFilePath = prefix + ".sym";
                    String errFilePath = prefix + ".err";

                    displayFileContent(outFilePath, outTextArea);
                    displayFileContent(symFilePath, symTextArea);
                    displayFileContent(errFilePath, errTextArea);
                    displayFileContent(inPath, inTextArea);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        JPanel topPanel = new JPanel();
        topPanel.add(codePathLabel);
        topPanel.add(codePathTextField);
        topPanel.add(browseButton);
        topPanel.add(compileButton);

        JPanel centerPanel = new JPanel();
        centerPanel.add(outLabel );
        centerPanel.add(outScrollPane);
        centerPanel.add(symLabel );
        centerPanel.add(symScrollPane);
        centerPanel.add(errLabel );
        centerPanel.add(errScrollPane);
        centerPanel.add(inLabel );
        centerPanel.add(inScrollPane);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);

        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        frame.setVisible(true);
    }

    public static void displayFileContent(String filePath, JTextArea textArea) {
        File file = new File(filePath);
        if (file.exists()) {
            // 在这里处理文件存在时的逻辑
            try {

                // 读取文件内容并显示在文本区域中
                FileReader reader = new FileReader(file);
                BufferedReader br = new BufferedReader(reader);
                String line;
                while ((line = br.readLine()) != null) {
                    textArea.append(line + "\n");
                }
                br.close();
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
