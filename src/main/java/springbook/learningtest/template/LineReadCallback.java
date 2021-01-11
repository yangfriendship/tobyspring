package springbook.learningtest.template;

import java.io.BufferedReader;
import java.io.IOException;

public interface LineReadCallback<T> {

    T doSomethingWithLine(String line, T value) throws IOException;
}
