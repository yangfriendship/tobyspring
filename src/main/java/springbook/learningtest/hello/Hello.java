package springbook.learningtest.hello;

public class Hello {

    private String name;

    private Printer printer;

    public String sayHello() {
        return "Hello " + name;
    }

    public void print(){
        this.printer.print(sayHello());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Printer getPrinter() {
        return printer;
    }

    public void setPrinter(Printer printer) {
        this.printer = printer;
    }
}
