import java.io.*;
import java.util.*;

public class MemoriaRestrita {
    public static void main(String[] args) throws IOException {
        // Lista com 27 células nulas (simula memória limitada)
        ArrayList<Object> data = new ArrayList<>(Collections.nCopies(27, null));

        // Carregar stop words
        BufferedReader bf = new BufferedReader(new FileReader(
                new File("C:\\Users\\pedro\\OneDrive\\Área de Trabalho\\projetos_java\\GoodOldTimes\\src\\StopWords")
        ));
        String[] stopWordsArray = bf.readLine().split(",");
        Set<String> stopWords = new HashSet<>(Arrays.asList(stopWordsArray));
        bf.close();

        data.set(0, stopWords);   // Stop words
        data.set(1, "");          // Linha atual
        data.set(2, -1);          // Índice de início da palavra
        data.set(3, 0);           // Índice do caractere atual
        data.set(4, false);       // Palavra encontrada
        data.set(5, "");          // Palavra atual
        data.set(6, "");          // Linha "palavra,contagem"
        data.set(7, 0);           // Frequência

        // Simula memória secundária
        RandomAccessFile wordFreqs = new RandomAccessFile("word_freqs", "rw");
        wordFreqs.setLength(0); // Limpa o arquivo

        // Lê o arquivo de entrada
        BufferedReader inputFile = new BufferedReader(new FileReader(
                new File("C:\\Users\\pedro\\OneDrive\\Área de Trabalho\\projetos_java\\GoodOldTimes\\src\\inputFile")
        ));
        String line;
        while ((line = inputFile.readLine()) != null) {
            line += "\n";
            data.set(1, line);
            data.set(2, -1);
            data.set(3, 0);

            for (char c : line.toCharArray()) {
                int start = (int) data.get(2);
                int i = (int) data.get(3);

                if (start == -1) {
                    if (Character.isLetterOrDigit(c)) {
                        data.set(2, i);
                    }
                } else {
                    if (!Character.isLetterOrDigit(c)) {
                        int end = i;
                        String word = line.substring(start, end).toLowerCase();
                        data.set(4, false);
                        data.set(5, word);

                        if (word.length() >= 2 && !stopWords.contains(word)) {
                            wordFreqs.seek(0);
                            String lineFromFile;
                            boolean found = false;

                            while ((lineFromFile = wordFreqs.readLine()) != null) {
                                String[] parts = lineFromFile.trim().split(",");
                                String existingWord = parts[0].trim();
                                int freq = Integer.parseInt(parts[1]);

                                if (existingWord.equals(word)) {
                                    freq++;
                                    data.set(4, true);
                                    data.set(7, freq);
                                    long pos = wordFreqs.getFilePointer();
                                    wordFreqs.seek(pos - 26);
                                    wordFreqs.writeBytes(String.format("%20s,%04d\n", word, freq));
                                    found = true;
                                    break;
                                }
                            }

                            if (!found) {
                                wordFreqs.seek(wordFreqs.length());
                                wordFreqs.writeBytes(String.format("%20s,%04d\n", word, 1));
                            }
                        }
                        data.set(2, -1); // Reinicia índice de palavra
                    }
                }
                data.set(3, i + 1); // Próximo caractere
            }
        }

        // Segunda parte: extrair as 25 palavras mais frequentes
        wordFreqs.seek(0); // Volta ao início do arquivo
        List<String[]> topWords = new ArrayList<>();

        String lineFromFile;
        while ((lineFromFile = wordFreqs.readLine()) != null) {
            String[] parts = lineFromFile.trim().split(",");
            if (parts.length == 2) {
                String word = parts[0].trim();
                int freq = Integer.parseInt(parts[1].trim());

                boolean inserted = false;
                for (int i = 0; i < topWords.size(); i++) {
                    if (freq > Integer.parseInt(topWords.get(i)[1])) {
                        topWords.add(i, new String[]{word, String.valueOf(freq)});
                        inserted = true;
                        break;
                    }
                }
                if (!inserted && topWords.size() < 25) {
                    topWords.add(new String[]{word, String.valueOf(freq)});
                }

                if (topWords.size() > 25) {
                    topWords = topWords.subList(0, 25);
                }
            }
        }

        // Exibe as 25 palavras mais frequentes
        System.out.println("\nTop 25 palavras:");
        for (String[] wordFreq : topWords) {
            System.out.println(wordFreq[0] + " - " + wordFreq[1]);
        }

        // Fecha os arquivos
        wordFreqs.close();
        inputFile.close();
    }
}
