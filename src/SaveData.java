import java.util.*;

/**
 * Data structures for saving game state
 */
public class SaveData {
    public int schemaVersion = 1;
    public String profileId;
    public String levelId;
    public long savedAtEpochMillis;
    public LevelSaveData levelData;
    // Persist which level number user is at
    public int currentLevelNumber;
}

class LevelSaveData {
    // Game state
    public boolean paused;
    public boolean gameOver;
    public boolean gameStarted;
    public double currentTime;
    public int coins;
    
    // Level state
    public double remainingWireLength;
    public int packetLoss;
    
    // Connection states
    public boolean circlesConnected;
    public boolean rectsConnected;
    
    // Stage-specific flags
    public boolean suppressImpact;
    public boolean suppressPacketLoss;
    
    // Node positions and connections
    public List<NodeSaveData> nodes = new ArrayList<>();
    public List<ConnectionSaveData> connections = new ArrayList<>();
    
    // Wire views with bends
    public List<WireSaveData> wireViews = new ArrayList<>();

    // Active packets mid-flow
    public List<PacketSaveData> packets = new ArrayList<>();
    
    // Flow generation state for levels
    public FlowStateSave flowState;
}

class FlowStateSave {
    // Level 1 specific
    public int triangleFlowsSent = 0;
    public int squareFlowsSent = 0;
    public boolean level1Complete = false;
    
    // Generic counters
    public int validSteam = 0;
    public int packetsGenerated = 0;
    public int packetsToGenerate = 0;
}

class NodeSaveData {
    public String id;
    public String type; // "CIRCLE", "SMALL_RECT", "BODY_RECT"
    public double x;
    public double y;
    public double width;  // for rectangles
    public double height; // for rectangles
    public double radius; // for circles
    public String color;  // color as hex string
    public int index;     // index in the respective list (circles, smallRects, bodies)
}

class ConnectionSaveData {
    public String node1Id;
    public String node2Id;
    public double lineStartX;
    public double lineStartY;
    public double lineEndX;
    public double lineEndY;
}

class WireSaveData {
    public String lineId; // identifier for the base line
    public double accountedLength;
    public double bendRadius;
    public List<BendData> bends = new ArrayList<>();
    
    static class BendData {
        public double x;
        public double y;
        public double maxRadius;
    }
}

class PacketSaveData {
    public String tag;           // "M_TRI", "M_SQ", "M_HEX"
    public String fromId;        // source node id
    public String toId;          // destination node id
    public double progress01;    // 0..1 fraction completed
    public double secondsTotal;  // total planned seconds
}
