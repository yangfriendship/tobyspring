package springbook.learningtest.template;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Calculator {

    public Integer sum(String path) throws IOException {
        return fileReadTemplate(path, new BufferedReaderCallback() {
            @Override
            public Integer doSomethingWithReader(BufferedReader br) throws IOException {
                Integer sum = 0;
                String line = null;
                while ((line = br.readLine()) != null) {
                    sum += Integer.parseInt(line);
                }
                return sum;
            }
        });
    }

    public Integer multiply(String filePath) throws IOException {
        return lineReadTemplate(filePath, new LineReadCallback<Integer>() {
            @Override
            public Integer doSomethingWithLine(String line, Integer value) throws IOException {
                return value * Integer.parseInt(line);
            }
        }, 1);
    }

    public String concatenate(String filepath) throws IOException {
        return lineReadTemplate(filepath, new LineReadCallback<String>() {
            @Override
            public String doSomethingWithLine(String line, String value) throws IOException {
                return value + line;
            }
        }, "");
    }

    private <T> T lineReadTemplate(String filepath, LineReadCallback<T> callback, T initVal)
        throws IOException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filepath));
            T result = initVal;
            String line = null;
            while ((line = br.readLine()) != null) {
                result = callback.doSomethingWithLine(line, result);
            }
            return result;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw e;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    throw e;
                }
            }
        }
    }

    private Integer fileReadTemplate(String filepath, BufferedReaderCallback callback)
        throws IOException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filepath));
            Integer sum = callback.doSomethingWithReader(br);
            return sum;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw e;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    throw e;
                }
            }
        }
    }

}
