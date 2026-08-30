/**
 * MediTime Web Audio API Sound Engine
 * Generates clear, loud, synthesized multi-tone alarm sounds without external audio file dependencies.
 */

class SoundEngine {
    constructor() {
        this.audioCtx = null;
        this.isPlaying = false;
        this.alarmInterval = null;
        this.volume = 0.8; // 0.0 to 1.0
        this.soundType = 'medical_beep'; // medical_beep, gentle_chime, pulse_siren, urgent_alert
        this.isAudioUnlocked = false;

        this.initEventListeners();
    }

    getAudioContext() {
        if (!this.audioCtx) {
            const AudioContextClass = window.AudioContext || window.webkitAudioContext;
            if (AudioContextClass) {
                this.audioCtx = new AudioContextClass();
            }
        }
        if (this.audioCtx && this.audioCtx.state === 'suspended') {
            this.audioCtx.resume();
        }
        return this.audioCtx;
    }

    unlockAudio() {
        const ctx = this.getAudioContext();
        if (ctx) {
            ctx.resume().then(() => {
                this.isAudioUnlocked = true;
                this.updateAudioIndicator(true);
                console.log("MediTime Sound Engine: AudioContext unlocked successfully.");
            }).catch(err => {
                console.warn("MediTime Sound Engine: Audio unlock failed:", err);
            });
        }
    }

    initEventListeners() {
        // Unlock on first user interaction anywhere
        const unlockHandler = () => {
            this.unlockAudio();
            document.removeEventListener('click', unlockHandler);
            document.removeEventListener('keydown', unlockHandler);
            document.removeEventListener('touchstart', unlockHandler);
        };
        document.addEventListener('click', unlockHandler);
        document.addEventListener('keydown', unlockHandler);
        document.addEventListener('touchstart', unlockHandler);
    }

    updateAudioIndicator(active) {
        const indicator = document.getElementById('audioStatusIndicator');
        if (indicator) {
            if (active) {
                indicator.className = 'badge bg-success audio-status-pill';
                indicator.innerHTML = '<i class="fa-solid fa-volume-high me-1"></i> Sound Alarms Active';
            } else {
                indicator.className = 'badge bg-warning text-dark audio-status-pill';
                indicator.innerHTML = '<i class="fa-solid fa-volume-xmark me-1"></i> Click to Enable Audio';
            }
        }
    }

    setVolume(volPercent) {
        this.volume = Math.max(0, Math.min(100, volPercent)) / 100.0;
    }

    setSoundType(type) {
        this.soundType = type || 'medical_beep';
    }

    playToneSequence() {
        const ctx = this.getAudioContext();
        if (!ctx) return;

        const masterGain = ctx.createGain();
        masterGain.gain.setValueAtTime(this.volume, ctx.currentTime);
        masterGain.connect(ctx.destination);

        const now = ctx.currentTime;

        switch (this.soundType) {
            case 'gentle_chime':
                // Polyphonic Pentatonic Chime: C5 (523Hz), E5 (659Hz), G5 (784Hz), C6 (1046.5Hz)
                [523.25, 659.25, 783.99, 1046.50].forEach((freq, index) => {
                    const osc = ctx.createOscillator();
                    const noteGain = ctx.createGain();
                    osc.type = 'sine';
                    osc.frequency.setValueAtTime(freq, now + index * 0.18);

                    noteGain.gain.setValueAtTime(0, now + index * 0.18);
                    noteGain.gain.linearRampToValueAtTime(0.4 * this.volume, now + index * 0.18 + 0.05);
                    noteGain.gain.exponentialRampToValueAtTime(0.001, now + index * 0.18 + 0.8);

                    osc.connect(noteGain);
                    noteGain.connect(masterGain);

                    osc.start(now + index * 0.18);
                    osc.stop(now + index * 0.18 + 0.85);
                });
                break;

            case 'pulse_siren':
                // Attention pulse: Alternating 650Hz and 950Hz
                for (let i = 0; i < 3; i++) {
                    const osc = ctx.createOscillator();
                    const noteGain = ctx.createGain();
                    osc.type = 'sawtooth';
                    const startTime = now + i * 0.35;

                    osc.frequency.setValueAtTime(650, startTime);
                    osc.frequency.linearRampToValueAtTime(950, startTime + 0.15);
                    osc.frequency.linearRampToValueAtTime(650, startTime + 0.3);

                    noteGain.gain.setValueAtTime(0.3 * this.volume, startTime);
                    noteGain.gain.exponentialRampToValueAtTime(0.001, startTime + 0.32);

                    osc.connect(noteGain);
                    noteGain.connect(masterGain);

                    osc.start(startTime);
                    osc.stop(startTime + 0.33);
                }
                break;

            case 'urgent_alert':
                // Fast staccato triple pulse (1200Hz)
                for (let i = 0; i < 4; i++) {
                    const osc = ctx.createOscillator();
                    const noteGain = ctx.createGain();
                    osc.type = 'square';
                    const startTime = now + i * 0.15;
                    osc.frequency.setValueAtTime(1200, startTime);

                    noteGain.gain.setValueAtTime(0.35 * this.volume, startTime);
                    noteGain.gain.exponentialRampToValueAtTime(0.001, startTime + 0.1);

                    osc.connect(noteGain);
                    noteGain.connect(masterGain);

                    osc.start(startTime);
                    osc.stop(startTime + 0.12);
                }
                break;

            case 'medical_beep':
            default:
                // Standard crisp hospital monitor beep (880Hz / 1760Hz double beep)
                for (let i = 0; i < 2; i++) {
                    const osc = ctx.createOscillator();
                    const noteGain = ctx.createGain();
                    osc.type = 'triangle';
                    const startTime = now + i * 0.22;
                    osc.frequency.setValueAtTime(i === 0 ? 880 : 1760, startTime);

                    noteGain.gain.setValueAtTime(0.5 * this.volume, startTime);
                    noteGain.gain.exponentialRampToValueAtTime(0.001, startTime + 0.18);

                    osc.connect(noteGain);
                    noteGain.connect(masterGain);

                    osc.start(startTime);
                    osc.stop(startTime + 0.2);
                }
                break;
        }
    }

    startAlarm() {
        if (this.isPlaying) return;
        this.unlockAudio();
        this.isPlaying = true;
        this.playToneSequence();

        // Repeat sound every 2.5 seconds until stopped
        this.alarmInterval = setInterval(() => {
            if (this.isPlaying) {
                this.playToneSequence();
            }
        }, 2500);
    }

    stopAlarm() {
        this.isPlaying = false;
        if (this.alarmInterval) {
            clearInterval(this.alarmInterval);
            this.alarmInterval = null;
        }
    }

    testSound(type, volume) {
        this.unlockAudio();
        const prevType = this.soundType;
        const prevVol = this.volume;

        if (type) this.soundType = type;
        if (volume !== undefined) this.setVolume(volume);

        this.playToneSequence();

        this.soundType = prevType;
        this.volume = prevVol;
    }
}

// Global singleton instance
window.soundEngine = new SoundEngine();
