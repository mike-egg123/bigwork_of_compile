import java.util.HashMap;

public class SymbolEntry {
    String type;
    int layer;
    boolean isConstant;
    String returnType = null;

    public InstructionEntry[] getInstructions() {
        return instructions;
    }

    public void setInstructions(InstructionEntry[] instrucions) {
        this.instructions = instrucions;
    }

    boolean isValid = true;
    InstructionEntry[] instructions = new InstructionEntry[1000];
    int instructionLen = 0;
    int locaVarCount = 0;
    int argVarCount = 1;
    HashMap<String, Integer> localVars = new HashMap<>();
    HashMap<String, Integer> argVars = new HashMap<>();
    boolean isParam = false;

    public int getArgVarCount() {
        return argVarCount;
    }

    public void setArgVarCount(int argVarCount) {
        this.argVarCount = argVarCount;
    }

    public HashMap<String, Integer> getArgVars() {
        return argVars;
    }

    public void setArgVars(HashMap<String, Integer> argVars) {
        this.argVars = argVars;
    }

    public boolean isParam() {
        return isParam;
    }

    public void setParam(boolean param) {
        isParam = param;
    }

    public HashMap<String, Integer> getLocalVars() {
        return localVars;
    }

    public void setLocalVars(HashMap<String, Integer> localVars) {
        this.localVars = localVars;
    }

    public int getLocaVarCount() {
        return locaVarCount;
    }

    public void setLocaVarCount(int locaVarCount) {
        this.locaVarCount = locaVarCount;
    }

    public int getInstructionLen() {
        return instructionLen;
    }

    public void setInstructionLen(int instructionLen) {
        this.instructionLen = instructionLen;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    boolean isInitialized;
    int stackOffset;

    /**
     * @param isConstant
     * @param isDeclared
     * @param stackOffset
     */
    public SymbolEntry(String type, int layer, boolean isConstant, boolean isDeclared, int stackOffset) {
        this.isConstant = isConstant;
        this.isInitialized = isDeclared;
        this.stackOffset = stackOffset;
        this.type = type;
        this.layer = layer;

    }
    public SymbolEntry(String type, String returnType, int layer, boolean isConstant, boolean isDeclared, int stackOffset) {
        this.isConstant = isConstant;
        this.isInitialized = isDeclared;
        this.stackOffset = stackOffset;
        this.type = type;
        this.layer = layer;
        this.returnType = returnType;
    }
    public SymbolEntry(String type, String returnType, InstructionEntry[] instructions, int layer, boolean isConstant, boolean isDeclared, int stackOffset) {
        this.isConstant = isConstant;
        this.isInitialized = isDeclared;
        this.stackOffset = stackOffset;
        this.type = type;
        this.layer = layer;
        this.returnType = returnType;
        this.instructions = instructions;
    }

    /**
     * @return the stackOffset
     */
    public int getStackOffset() {
        return stackOffset;
    }

    /**
     * @return the isConstant
     */
    public boolean isConstant() {
        return isConstant;
    }

    /**
     * @return the isInitialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * @param isConstant the isConstant to set
     */
    public void setConstant(boolean isConstant) {
        this.isConstant = isConstant;
    }

    /**
     * @param isInitialized the isInitialized to set
     */
    public void setInitialized(boolean isInitialized) {
        this.isInitialized = isInitialized;
    }

    /**
     * @param stackOffset the stackOffset to set
     */
    public void setStackOffset(int stackOffset) {
        this.stackOffset = stackOffset;
    }
}
