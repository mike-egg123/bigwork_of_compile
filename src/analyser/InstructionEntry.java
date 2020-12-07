package analyser;

public class InstructionEntry {
    String instru;
    int opera = -1000;

    public String getInstru() {
        return instru;
    }

    public void setInstru(String instru) {
        this.instru = instru;
    }

    public int getOpera() {
        return opera;
    }

    public void setOpera(int opera) {
        this.opera = opera;
    }

    public InstructionEntry(String instru){
        this.instru = instru;
    }
    public InstructionEntry(String instru, int opera){
        this.instru = instru;
        this.opera = opera;
    }
}
