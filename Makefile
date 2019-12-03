JC	     = javac
RUNFLAGS = -Djava.security.policy=policy 

OBJECTS = src/*.java
TARGET  = classes
CLASS   = $(shell find . -type f -name "*.class" | tr '\n' ' ' | sed 's/\$$/\\$$/g')
.SUFFIXES: .java .class

.java.class:
		$(JC) $(JCFLAGS) $*.java

all: $(TARGET)

$(TARGET): $(OBJECTS:.java=.class)

runS:
		java $(RUNFLAGS) $(JCFLAGS) Server 1997

runC:
		java $(RUNFLAGS) DetectionNode

clean:
		$(RM) $(CLASS)
