# Maze Creator Enhancement Project

## Project Overview

This project involves enhancing an existing Java Swing maze editor to create complete, game-ready mazes for the **Maze Runner** multiplayer game system. The current editor only supports basic maze creation (walls, floors, player starts, finish lines) but needs significant expansion to support the full game mechanics.

## Current State Analysis

### Existing Functionality ✅
- **Basic Maze Editor**: 10x10 to 500x500 grid support
- **Cell Types**: FLOOR, WALL, START (@), FINISH (!)
- **Player Support**: 1-8 players with color-coded start/finish positions
- **UI Features**: Click-and-drag editing, zoom (Ctrl+scroll), grid resizing
- **I/O Operations**: JSON save/load with basic format
- **Generation**: Experimental labyrinth generator

### Current File Structure
```
src/
├── net.simplehardware/
│   ├── CellButton.java      - Individual maze cell UI component
│   ├── MazeEditor.java      - Main application window
│   ├── MazeGrid.java        - Grid management and zoom functionality
│   ├── MazeInfoData.java    - Data structure for JSON serialization
│   ├── MazeIO.java          - File save/load operations
│   ├── Mode.java            - Enum defining cell types
│   └── ToolbarFactory.java  - UI toolbar creation
```

## Target Game Requirements

### Maze Runner Game Mechanics
The maze editor must support a sophisticated multiplayer game where:

1. **Form Collection**: Players collect forms A, B, C... in alphabetical order
2. **Player Ownership**: Each form belongs to a specific player (1-8)
3. **Sequential Gameplay**: Players must collect forms in exact sequence
4. **Multiple Levels**: Game has 5 league levels with increasing complexity
5. **Sheet System**: Level 5+ includes moveable sheet objects
6. **JSON Integration**: Must export mazes compatible with game engine

### Required Maze Format
The game engine expects this cell encoding:
```
[CellType][OwnerID]
- @1 = Player 1 start position
- !2 = Player 2 finish line
- A3 = Form A belonging to player 3
- B1 = Form B belonging to player 1
- #0 = Wall (no owner)
- S0 = Sheet object (Level 5+)
```

### JSON Structure Required
```json
{
  "id": "MazeA",
  "name": "Training Maze",
  "forms": [
    {"id": "A", "name": "Application Form"},
    {"id": "B", "name": "Registration Form"},
    {"id": "C", "name": "Final Assessment"}
  ],
  "maze": "A1B1 2@1#0C1!1/..."
}
```

## Required Enhancements

### CRITICAL: Form System Implementation

#### 1. Expand Mode Enum
**File**: `Mode.java`
```java
public enum Mode {
    FLOOR, WALL, START, FINISH,
    // Add all form types
    FORM_A, FORM_B, FORM_C, FORM_D, FORM_E, FORM_F, FORM_G, FORM_H,
    FORM_I, FORM_J, FORM_K, FORM_L, FORM_M, FORM_N, FORM_O, FORM_P,
    FORM_Q, FORM_R, FORM_S, FORM_T, FORM_U, FORM_V, FORM_W, FORM_X,
    FORM_Y, FORM_Z,
    SHEET  // For Level 5+ gameplay
}
```

#### 2. Enhanced Cell Rendering
**File**: `CellButton.java`
- Add visual representation for forms A-Z
- Display form letter and player color
- Show sheet objects with distinct styling
- Implement player-specific color coding

#### 3. Form Toolbar Creation
**File**: `ToolbarFactory.java`
- Create secondary toolbar with A-Z form buttons
- Add form sequence editor dialog
- Include sheet placement tools
- Add form validation tools

#### 4. Data Structure Updates
**File**: `MazeInfoData.java`
```java
public class MazeInfoData {
    public String id;
    public String name;
    public List<FormInfo> forms;  // NEW: Form definitions
    public String maze;
    public int maxPlayers = 4;    // NEW: Player limit
    public int recommendedLevel = 2; // NEW: Difficulty level
}

// NEW CLASS NEEDED
public class FormInfo {
    public char id;      // 'A', 'B', 'C', etc.
    public String name;  // Human-readable form name
}
```

#### 5. Enhanced Export Format
**File**: `MazeIO.java`
- Update maze string generation to include form encodings
- Support form definitions in JSON export
- Validate maze completeness before export
- Add import functionality for existing game mazes

### IMPORTANT: Validation System

#### 6. Maze Validator (NEW FILE)
**File**: `MazeValidator.java`
```java
public class MazeValidator {
    public ValidationResult validateMaze(MazeGrid grid);
    public boolean checkPlayerBalance(int playerId);
    public boolean verifyFormSequence(int playerId);
    public boolean checkPathReachability();
}
```

Requirements:
- Each player must have exactly one start and one finish
- Each player must have complete form sequence (A, B, C... with no gaps)
- All forms must be reachable from player start positions
- No duplicate forms for same player
- Warn about balance issues between players

#### 7. Enhanced UI Feedback
- Real-time validation indicators
- Player-specific form counters
- Balance warnings
- Path reachability visualization

### RECOMMENDED: Advanced Features

#### 8. Maze Templates (NEW FILE)
**File**: `MazeTemplates.java`
- Pre-built maze layouts for each league level
- Balanced 2-8 player configurations
- Quick-start templates for testing

#### 9. Form Sequence Editor (NEW FILE)
**File**: `FormSequenceDialog.java`
- Visual form sequence designer
- Form naming interface
- Player assignment tools
- Bulk operations (assign all A forms to players, etc.)

#### 10. Preview Mode
- Test maze playability
- Simulate player paths
- Visualize form collection order
- Export preview as image

## Implementation Strategy

### Phase 1: Core Forms Support
**Priority**: CRITICAL
**Timeline**: 2-3 days

1. Expand `Mode.java` with all form types
2. Update `CellButton.java` rendering for forms
3. Add basic form toolbar to `ToolbarFactory.java`
4. Test form placement and visual representation

### Phase 2: Data Integration
**Priority**: CRITICAL
**Timeline**: 2-3 days

1. Update `MazeInfoData.java` structure
2. Modify `MazeIO.java` export format
3. Implement proper maze string encoding
4. Test save/load with form data

### Phase 3: Validation
**Priority**: IMPORTANT
**Timeline**: 3-4 days

1. Create `MazeValidator.java`
2. Add real-time validation to UI
3. Implement balance checking
4. Add user feedback systems

### Phase 4: Polish
**Priority**: NICE-TO-HAVE
**Timeline**: 2-3 days

1. Add templates and presets
2. Implement form sequence editor
3. Add preview functionality
4. Optimize user experience

## Technical Specifications

### Form Rendering Guidelines
- **Colors**: Each player gets distinct color (use HSB color wheel)
- **Form Display**: Show letter prominently with small player number
- **Sheet Display**: Yellow/gold color with "S" symbol
- **Hover Effects**: Show form details on mouse over

### Validation Rules
```
For each player (1-8):
- Exactly 1 start position (@N)
- Exactly 1 finish position (!N)
- Forms A-Z in sequence (no gaps: if C exists, A and B must exist)
- All forms reachable from start position
- Finish reachable after collecting all forms
```

### Export Format Details
Maze string uses "/" to separate rows, each cell is 2 characters:
```
"A1B1C1 2#0#0!1/D1E1F1 2#0#0 1/@1G1H1 2#0#0 1"
```
Translation:
- Row 1: FormA-Player1, FormB-Player1, FormC-Player1, Floor, Wall, Wall, Finish-Player1
- Row 2: FormD-Player1, FormE-Player1, FormF-Player1, Floor, Wall, Wall, Floor
- Row 3: Start-Player1, FormG-Player1, FormH-Player1, Floor, Wall, Wall, Floor

## Testing Requirements

### Unit Tests Needed
1. **Mode enum** expansion verification
2. **Cell encoding/decoding** accuracy
3. **JSON serialization** round-trip testing
4. **Maze validation** rule checking
5. **Form sequence** logic verification

### Integration Tests
1. **Complete maze** creation workflow
2. **Save/load** functionality with forms
3. **Multi-player** maze validation
4. **Game engine** compatibility testing

### Manual Testing Checklist
- [ ] Can place all form types A-Z
- [ ] Form player assignment works correctly
- [ ] Visual rendering is clear and distinct
- [ ] Save/load preserves all form data
- [ ] Validation catches common errors
- [ ] Export format matches game engine requirements
- [ ] Multi-player mazes balance correctly

## Known Issues & Considerations

### Current Limitations
1. **No form support** - Primary blocking issue
2. **Limited validation** - Only basic cell type checking
3. **Simple export format** - Missing form encoding
4. **No game engine testing** - Compatibility unknown

### Design Decisions Needed
1. **Form limit per player** - Should there be a maximum?
2. **Sheet quantity** - How many sheets per maze?
3. **Maze size limits** - Optimal range for different player counts?
4. **Template complexity** - How many pre-built templates?

### Dependencies
- **Gson**: Already included for JSON handling
- **Swing**: Core UI framework (already in use)
- **Java 11+**: Required for modern language features

## Getting Started

### Development Environment
1. Clone/access existing maze creator project
2. Ensure Java 11+ development environment
3. Import into IDE (IntelliJ IDEA/Eclipse recommended)
4. Run `MazeEditor.main()` to see current functionality

### First Steps
1. **Study existing code** - Understand current architecture
2. **Review game requirements** - See `BOT_REQUIREMENTS.md` for context
3. **Start with Mode enum** - Expand to include forms
4. **Test incrementally** - Verify each change doesn't break existing functionality

This enhancement will transform a basic maze editor into a full-featured tool for creating complex, game-ready mazes that support the complete Maze Runner gameplay experience.
