import speech_recognition as sr
import sys

def main():
    if len(sys.argv) < 2:
        print("No file")
        return
    file_path = sys.argv[1]
    r = sr.Recognizer()
    r.energy_threshold = 50  # Lower threshold to pick up quieter speech
    try:
        with sr.AudioFile(file_path) as source:
            r.adjust_for_ambient_noise(source, duration=0.2)
            audio = r.record(source)
        text = r.recognize_google(audio)
        print(text)
    except sr.UnknownValueError:
        print("Error: No speech detected")
    except Exception as e:
        print("Error: " + str(e))

if __name__ == '__main__':
    main()
