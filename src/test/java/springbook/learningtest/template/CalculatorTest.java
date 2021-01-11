package springbook.learningtest.template;

import java.io.IOException;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

public class CalculatorTest extends TestCase {

    private Calculator calculator;
    private String filePath;

    @Before
    public void setUp() {
        this.calculator = new Calculator();
        this.filePath = getClass().getResource("/numbers.txt").getPath();
    }

    @Test
    public void testSumOfNumbers() throws IOException {
        int sum = calculator.sum(filePath);
        Assert.assertEquals(10, sum);
    }

    @Test
    public void testMultiplyTest() throws IOException {
        int multiply = calculator.multiply(filePath);
        Assert.assertEquals(24, multiply);
    }

    @Test
    public void testConcatenateTest() throws IOException {
        String result = calculator.concatenate(filePath);
        Assert.assertEquals("1234",result);
    }

}