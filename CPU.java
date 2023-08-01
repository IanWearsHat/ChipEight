import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Stack;
import java.util.function.IntConsumer;

public class CPU {
    Renderer renderer;
    Keyboard keyboard;
    Speaker speaker;

    int[] memory = new int[4096]; // 4096 bytes (4KB) of memory
    int[] v = new int[16]; // 16 8-bit registers

    int i = 0; // stores memory addresses

    int delayTimer = 0;
    int soundTimer = 0;

    int pc = 0x200; // Program counter. Stores the currently executing address.

    Stack<Integer> stack = new Stack<>();

    boolean paused = false;

    int speed = 10;

    boolean debug = false;

    public CPU(Renderer renderer, Keyboard keyboard, Speaker speaker) {
        this.renderer = renderer;
        this.keyboard = keyboard;
        this.speaker = speaker;
    }

    private int lastEightBits(int num) {
        return num & 0xff;
    }

    public int[] convertBytesToUnsignedInt(byte[] data) {
        int[] unsigned = new int[data.length];

        for (int i = 0; i < data.length; i++) {
            unsigned[i] = data[i] & 0xff;
        }

        return unsigned;
    }

    public void loadSpritesIntoMemory() {
        int[] sprites = {
            0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
            0x20, 0x60, 0x20, 0x20, 0x70, // 1
            0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
            0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
            0x90, 0x90, 0xF0, 0x10, 0x10, // 4
            0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
            0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
            0xF0, 0x10, 0x20, 0x40, 0x40, // 7
            0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
            0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
            0xF0, 0x90, 0xF0, 0x90, 0x90, // A
            0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
            0xF0, 0x80, 0x80, 0x80, 0xF0, // C
            0xE0, 0x90, 0x90, 0x90, 0xE0, // D
            0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
            0xF0, 0x80, 0xF0, 0x80, 0x80  // F
        };

            // According to the technical reference, sprites are stored in the interpreter section of memory starting at hex 0x000
        for (int i = 0; i < sprites.length; i++) {
            this.memory[i] = lastEightBits(sprites[i]);
        }
    }

    public void loadProgramIntoMemory(int[] program) {
        for (int loc = 0; loc < program.length; loc++) {
            this.memory[0x200 + loc] = program[loc];
        }
    }

    public void loadRom(String romPath) {
        Path path = Paths.get(romPath);

        try {
            byte[] data = Files.readAllBytes(path);
            int[] program = this.convertBytesToUnsignedInt(data);

            this.loadProgramIntoMemory(program);

            if (debug) {
                int offset = 2;
                System.out.println(data[0 + offset]);
                System.out.println(program[0 + offset]);
                System.out.println(this.memory[0x200 + offset]);
            }

        }
        catch (IOException e) {
            System.out.println("Failed reading rom.");
        }

    }

    private void updateTimers() {
        if (this.delayTimer > 0) {
            this.delayTimer -= 1;
        }
    
        if (this.soundTimer > 0) {
            this.soundTimer -= 1;
        }
    }

    private void playSound() {
        // if (this.soundTimer > 0) {
        //     this.speaker.playFrequency(440);
        // } else {
        //     this.speaker.stop();
        // }
    }

    private void executeInstruction(int opcode) {
        this.pc += 2;

        int x = (opcode & 0x0F00) >> 8;
        int y = (opcode & 0x00F0) >> 4;

        switch (opcode & 0xF000) {
            case 0x0000:
                switch (opcode) {
                    case 0x00E0:
                        this.renderer.clear();
                        break;
                    case 0x00EE:
                        this.pc = this.stack.pop();
                        break;
                }
        
                break;
            case 0x1000:
                this.pc = (opcode & 0xFFF);
                break;
            case 0x2000:
                this.stack.push(this.pc);
                this.pc = (opcode & 0xFFF);
                break;
            case 0x3000:
                if (this.v[x] == (opcode & 0xFF)) {
                    this.pc += 2;
                }
                break;
            case 0x4000:
                if (this.v[x] != (opcode & 0xFF)) {
                    this.pc += 2;
                }
                break;
            case 0x5000:
                if (this.v[x] == this.v[y]) {
                    this.pc += 2;
                }
                break;
            case 0x6000:
                this.v[x] = (opcode & 0xFF);
                break;
            case 0x7000:
                this.v[x] += (opcode & 0xFF);
                break;
            case 0x8000:
                switch (opcode & 0xF) {
                    case 0x0:
                        this.v[x] = this.v[y];
                        break;
                    case 0x1:
                        this.v[x] |= this.v[y];
                        break;
                    case 0x2:
                        this.v[x] &= this.v[y];
                        break;
                    case 0x3:
                        this.v[x] ^= this.v[y];
                        break;
                    case 0x4:
                        int sum = (this.v[x] += this.v[y]);

                        this.v[0xF] = 0;

                        if (sum > 0xFF) {
                            this.v[0xF] = 1;
                        }

                        this.v[x] = sum;
                        break;
                    case 0x5:
                        this.v[0xF] = 0;

                        if (this.v[x] > this.v[y]) {
                            this.v[0xF] = 1;
                        }

                        this.v[x] -= this.v[y];
                        break;
                    case 0x6:
                        this.v[0xF] = (this.v[x] & 0x1);

                        this.v[x] >>= 1;
                        break;
                    case 0x7:
                        this.v[0xF] = 0;

                        if (this.v[y] > this.v[x]) {
                            this.v[0xF] = 1;
                        }

                        this.v[x] = this.v[y] - this.v[x];
                        break;
                    case 0xE:
                        this.v[0xF] = (this.v[x] & 0x80);
                        this.v[x] <<= 1;
                        break;
                }
        
                break;
            case 0x9000:
                if (this.v[x] != this.v[y]) {
                    this.pc += 2;
                }
                break;
            case 0xA000:
                this.i = (opcode & 0xFFF);
                break;
            case 0xB000:
                this.pc = (opcode & 0xFFF) + this.v[0];
                break;
            case 0xC000:
                int rand = (int) Math.floor(Math.random() * 0xFF);

                this.v[x] = rand & (opcode & 0xFF);
                break;
            case 0xD000:
                int width = 8;
                int height = (opcode & 0xF);
            
                this.v[0xF] = 0;
            
                for (int row = 0; row < height; row++) {
                    int sprite = this.memory[this.i + row];
            
                    for (int col = 0; col < width; col++) {
                        // If the bit (sprite) is not 0, render/erase the pixel
                        if ((sprite & 0x80) > 0) {
                            // If setPixel returns 1, which means a pixel was erased, set VF to 1
                            if (this.renderer.setPixel(this.v[x] + col, this.v[y] + row)) {
                                this.v[0xF] = 1;
                            }
                        }
            
                        // Shift the sprite left 1. This will move the next next col/bit of the sprite into the first position.
                        // Ex. 10010000 << 1 will become 0010000
                        sprite <<= 1;
                    }
                }
            
                break;
            case 0xE000:
                switch (opcode & 0xFF) {
                    case 0x9E:
                        if (this.keyboard.isKeyPressed(this.v[x])) {
                            this.pc += 2;
                        }
                        break;
                    case 0xA1:
                        if (!this.keyboard.isKeyPressed(this.v[x])) {
                            this.pc += 2;
                        }
                        break;
                }
        
                break;
            case 0xF000:
                switch (opcode & 0xFF) {
                    case 0x07:
                        this.v[x] = this.delayTimer;
                        break;
                    case 0x0A:
                        this.paused = true;

                        this.keyboard.onNextKeyPress = key -> this.v[x] = key;
                        IntConsumer unpause = key -> this.paused = false;

                        this.keyboard.onNextKeyPress.andThen(unpause);

                        break;
                    case 0x15:
                        this.delayTimer = this.v[x];
                        break;
                    case 0x18:
                        this.soundTimer = this.v[x];
                        break;
                    case 0x1E:
                        this.i += this.v[x];
                        break;
                    case 0x29:
                        this.i = this.v[x] * 5;
                        break;
                    case 0x33:
                        // Get the hundreds digit and place it in I.
                        this.memory[this.i] = (int) (this.v[x] / 100);

                        // Get tens digit and place it in I+1. Gets a value between 0 and 99,
                        // then divides by 10 to give us a value between 0 and 9.
                        this.memory[this.i + 1] = (int) ((this.v[x] % 100) / 10);

                        // Get the value of the ones (last) digit and place it in I+2.
                        this.memory[this.i + 2] = (int) (this.v[x] % 10);
                        break;
                    case 0x55:
                        for (int registerIndex = 0; registerIndex <= x; registerIndex++) {
                            this.memory[this.i + registerIndex] = this.v[registerIndex];
                        }
                        break;
                    case 0x65:
                        for (int registerIndex = 0; registerIndex <= x; registerIndex++) {
                            this.v[registerIndex] = this.memory[this.i + registerIndex];
                        }
                        break;
                }
        
                break;
        
            default:
                throw new Error("Unknown opcode " + opcode);
        }
    }

    public void cycle() {
        for (int i = 0; i < this.speed; i++) {
            if (!this.paused) {
                int opcode = (this.memory[this.pc] << 8 | this.memory[this.pc + 1]);
                this.executeInstruction(opcode);
            }
        }
    
        if (!this.paused) {
            this.updateTimers();
        }
    
        this.playSound();
    }
}
