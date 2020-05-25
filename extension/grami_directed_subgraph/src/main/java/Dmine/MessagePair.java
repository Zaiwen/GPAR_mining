package Dmine;

public class MessagePair {
    private Message a;
    private Message b;
    private double diversification;

    public MessagePair(Message a, Message b) {
        this.a = a;
        this.b = b;
    }

    public void setDiversification(double diversification) {
        this.diversification = diversification;
    }

    public double getDiversification() {
        return diversification;
    }

    @Override
    public int hashCode() {
        return this.a.hashCode() * this.b.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        MessagePair m = (MessagePair) obj;
        return (this.a.getId().equals(m.a.getId()) && this.b.getId().equals(m.b.getId()))
                || (this.a.getId().equals(m.b.getId()) && this.b.getId().equals(m.a.getId()));
    }

    public Message getA() {
        return a;
    }

    public Message getB() {
        return b;
    }
}
