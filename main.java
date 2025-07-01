import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

//---Queue Implementation-----

//Class for Queue Node (Will Contain Single PCB and Point to Next Queue Node)
class QNode {

	private PCB data;
	private QNode next;

	// Getters and Setters
	public PCB getData() {
		return data;
	}

	public void setData(PCB data) {
		this.data = data;
	}

	public QNode getNext() {
		return next;
	}

	public void setNext(QNode next) {
		this.next = next;
	}

};

//Queue For FCFS (Enqueue at Rear, Dequeue from Front)
class Queue {

	private QNode front;
	private QNode rear;
	char mode;

	// Constructor to Initialize Empty Queue
	public Queue() {

		front = null;
		rear = null;

	}

	public Queue(char m) {

		mode = m;
		front = null;
		rear = null;

	}

	// Getter for Queue Front
	public QNode getFront() {

		return front;
	}

	// Inserting at Rear
	void Enqueue(PCB val) {

		QNode newNode = new QNode();
		newNode.setNext(null);
		newNode.setData(val);

		if (isEmpty()) {

			front = newNode;
			rear = newNode;
		}

		else {

			// If FCFS or RR Enqueue by First Come First Serve

			if (mode == 'f' || mode == 'r') {
				rear.setNext(newNode);
				rear = newNode;
			}

			// If SJF Enqueue According to Burst
			else if (mode == 's') {

				if (front.getData().getBurst() > val.getBurst()) {

					newNode.setNext(front);
					front = newNode;
				}

				else {

					QNode curr = front;

					while (curr.getNext() != null && curr.getNext().getData().getBurst() <= val.getBurst())
						curr = curr.getNext();

					newNode.setNext(curr.getNext());
					curr.setNext(newNode);
				}

			}

		}
	}

	// Incrementing Waiting Time in Queue for Processes
	public void incrementWaitingTime() {

		QNode curr = front;

		while (curr != null) {

			curr.getData().incWaitingTime();
			curr = curr.getNext();
		}

	}

	// Removing from Front
	PCB Dequeue() {

		if (isEmpty()) {
			return null;
		}

		else {
			QNode temp = front;
			PCB val = temp.getData();
			front = front.getNext();

			if (isEmpty())
				rear = null;

			return val;

		}
	}

	// Checking if Queue is Empty
	public boolean isEmpty() {

		return front == null;
	}

};

//Class for PCB (Contains all Info of a Single Process/Job)
class PCB {

	private int process_id;
	private int burst;
	private int start_time;
	private int memory;
	private int termination_time;
	private int turnaround_time;
	private int waiting_time;
	private int response_time;
	private int remaining_time;
	private int currTimeCPU;
	private boolean queued;

	// Default Constructor
	public PCB() {

		process_id = 0;
		this.start_time = -1;
		this.termination_time = 0;
		this.memory = 0;
		this.waiting_time = 0;
		this.response_time = 0;
		this.turnaround_time = 0;
		this.remaining_time = this.burst;
		queued = false;
	}

	// Initializing PCB With ID, Burst, Memory
	public PCB(int i, int b, int mem) {
		this.process_id = i;
		this.burst = b;
		this.memory = mem;
		this.start_time = -1;
		this.termination_time = 0;
		this.waiting_time = 0;
		this.response_time = 0;
		this.turnaround_time = 0;
		this.remaining_time = this.burst;
		queued = false;
	}

	// Getter and Setters for PCB
	public int getProcess_id() {
		return process_id;
	}

	public void setProcess_id(int process_id) {
		this.process_id = process_id;
	}

	public int getMemory() {
		return memory;
	}

	public void setMemory(int memory) {
		this.memory = memory;
	}

	public int getBurst() {
		return burst;
	}

	public void setBurst(int burst) {
		this.burst = burst;
	}

	public int getStart_time() {
		return start_time;
	}

	public void setStart_time(int start_time) {
		this.start_time = start_time;
	}

	public int getTermination_time() {
		return termination_time;
	}

	public void setTermination_time(int termination_time) {
		this.termination_time = termination_time;
	}

	public int getTurnaround_time() {
		return turnaround_time;
	}

	public void setTurnaround_time(int turnaround_time) {
		this.turnaround_time = turnaround_time;
	}

	public int getWaiting_time() {
		return waiting_time;
	}

	public void setWaiting_time(int waiting_time) {
		this.waiting_time = waiting_time;
	}

	public int getResponse_time() {
		return response_time;
	}

	public void setResponse_time(int response_time) {
		this.response_time = response_time;
	}

	public int getRemaining_time() {
		return remaining_time;
	}

	public void setRemaining_time(int remaining_time) {
		this.remaining_time = remaining_time;
	}

	public int getCurrTimeCPU() {
		return currTimeCPU;
	}

	public void setCurrTimeCPU(int currTimeCPU) {
		this.currTimeCPU = currTimeCPU;
	}

	public boolean isQueued() {
		return queued;
	}

	public void setQueued(boolean queued) {
		this.queued = queued;
	}

	// Function to Increment Waiting Time When Process is in Queue
	public void incWaitingTime() {
		this.waiting_time++;
	}

	// Function to Increment Time at CPU
	public void incCurrTimeCPU() {
		this.currTimeCPU++;
	}

	// Function to Decrement Burst Remaining
	public void decRemainingTime() {
		this.remaining_time--;
	}

}

//Class for Reading Processes From File to Job Queue
class JobRead extends Thread {

	char mode;
	private ArrayList<PCB> job_queue;

	public JobRead(char m) {

		mode = m;
	}

	// Thread to Read Processes From File to Job Queue
	public void run() {

		job_queue = new ArrayList<PCB>();
		int p, b, m;

		try {
			FileReader fr;
			fr = new FileReader("job.txt");

			Scanner freader = new Scanner(fr);
			String data;

			// Reading each line and Adding to Job Queue
			while (freader.hasNextLine()) {
				data = freader.nextLine();

				// Splitting a line based on : and ; to get Process Id, burst and memory
				// required
				String list1[] = data.split(":");
				String list2[] = list1[1].split(";");

				// String to Integer Conversion
				p = Integer.parseInt(list1[0]);
				b = Integer.parseInt(list2[0]);
				m = Integer.parseInt(list2[1]);

				// Pushing to Job Queue
				job_queue.add(new PCB(p, b, m));

			}
			freader.close();

			// Sorting the PRocess by Their Burst if SJF
			if (mode == 's') {

				for (int i = 0; i < job_queue.size() - 1; i++) {

					for (int j = 0; j < job_queue.size() - i - 1; j++) {

						if (job_queue.get(j).getBurst() > job_queue.get(j + 1).getBurst()) {

							PCB temp = job_queue.get(j);
							job_queue.set(j, job_queue.get(j + 1));
							job_queue.set(j + 1, temp);

						}

					}

				}

			}

			for (int i = 0; i < job_queue.size(); i++)
				System.out
						.println("=>Process with ID = " + job_queue.get(i).getProcess_id() + " Added to Job Queue!!!");

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public char getMode() {
		return mode;
	}

	public void setMode(char mode) {
		this.mode = mode;
	}

	public ArrayList<PCB> getJob_queue() {
		return job_queue;
	}

	public void setJob_queue(ArrayList<PCB> job_queue) {
		this.job_queue = job_queue;
	}

}

class GanttEvent {

	int id;;
	int start;
	int end;

	public GanttEvent(int id, int start, int end) {
		super();
		this.id = id;
		this.start = start;
		this.end = end;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

}

//Class for Our FCFS Scheduler Extends Thread
class Scheduler extends Thread {

	private ArrayList<PCB> job_queue;
	private Queue ready_queue;
	private ArrayList<GanttEvent> gantt_chart;
	private PCB curr_process;
	Semaphore sem;
	Semaphore sem1;
	Semaphore sem2;
	Semaphore sem3;
	int currentTime;
	int pushed;
	int totalMemory;
	int memoryUsed;
	int quantum;
	int last_start;
	char mode;

	// Independent Thread to Add Processes to Ready Queue When Memory Available
	public void run() {

		pushed = 0;
		// Reading Threads for File "job.txt"
		// Creating Scheduler Object
		JobRead jr = new JobRead(mode);

		// ---Starting Independent Thread For Reading Processes From File to Job Queue
		jr.start();
		while (jr.isAlive())
			;
		job_queue = jr.getJob_queue();
		// Releasing Conditional Semaphore to Start Simulation
		sem.release();

		// Dispatching Processes From Job Queue to Ready Queue Which have Memory <
		// Available Memory
		while (pushed != job_queue.size()) {
			// Semaphore Synchronization Between This Thread and Main Thread to Match
			// Current Time
			try {
				sem1.acquire();
				sem3.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Checking for Processes with Memory < Available Memory and Adding them to
			// Ready Queue
			checkSpace();

			// Releasing Semaphores
			sem3.release();
			sem2.release();

		}

	}

	// Initializing Scheduler Attributes
	public Scheduler(char m, int q) {

		quantum = q;
		mode = m;
		job_queue = new ArrayList<PCB>();
		gantt_chart = new ArrayList<GanttEvent>();
		curr_process = null;
		ready_queue = new Queue(m);
		currentTime = 0;
		totalMemory = 1024;
		memoryUsed = 0;
		sem = new Semaphore(0);
		sem1 = new Semaphore(1);
		sem2 = new Semaphore(0);
		sem3 = new Semaphore(1);
	}

	// Getter for Job Queue
	public ArrayList<PCB> getJob_Queue() {
		return job_queue;
	}

	// Setter for Job Queue
	public void setJob_Queue(ArrayList<PCB> job_queue) {
		this.job_queue = job_queue;
	}

	// Mutually Exclusive Function to Check Processes That Can be Added to Ready
	// Queue
	public synchronized void checkSpace() {

		for (int i = 0; i < job_queue.size(); i++) {

			if (!job_queue.get(i).isQueued()) {

				ready_queue.Enqueue(job_queue.get(i));
				job_queue.get(i).setQueued(true);
				memoryUsed += job_queue.get(i).getMemory();
				System.out.println("=>Process with ID = " + job_queue.get(i).getProcess_id()
						+ " Added to Ready Queue at Time " + currentTime);
				pushed++;
			}

		}

	}

	// Simulation for FCFS,SJF, and RR-10 scheduling
	public void simulateScheduling() {

		int completed = 0;

		// Semaphore to Wait Until Job Queue is Populated
		try {
			sem.acquire();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Simulation Loop Until All Processes Terminated
		while (completed != job_queue.size()) {

			// Semaphore Synchronization Between This Thread and Independent Job Dispatcher
			// Thread to Match Current Time
			try {
				sem2.acquire();
				sem3.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Checking For Process Termination
			if (curr_process != null && curr_process.getRemaining_time() == 0) {

				curr_process.setTermination_time(currentTime);
				// Releasing Memory
				memoryUsed -= curr_process.getMemory();
				// Adding Terminated Process to Gantt Chart
				gantt_chart.add(new GanttEvent(curr_process.getProcess_id(), last_start, currentTime));
				completed++;
				curr_process = null;

			}

			// Checking if CPU IDLE
			if (curr_process == null) {

				if (!ready_queue.isEmpty()) {

					// Sending Process to CPU
					curr_process = ready_queue.Dequeue();
					// Adding Scheduled Process to Gantt Chart
					last_start = currentTime;
					curr_process.setCurrTimeCPU(0);

					// Setting Start Time of Current Process if Not Already Started
					if (curr_process.getStart_time() == -1)
						curr_process.setStart_time(currentTime);

				}
			}

			// Only For Round Robin
			if (mode == 'r') {

				// Checking For Timeout
				if (curr_process != null && curr_process.getCurrTimeCPU() == quantum) {

					// Enqueueing Current Process and Adding it to Gantt Chart
					ready_queue.Enqueue(curr_process);
					gantt_chart.add(new GanttEvent(curr_process.getProcess_id(), last_start, currentTime));

					if (!ready_queue.isEmpty()) {

						// Choosing New Process for CPU
						curr_process = ready_queue.Dequeue();
						last_start = currentTime;
						curr_process.setCurrTimeCPU(0);

						// Checking If It's The First Response for a Process
						if (curr_process.getStart_time() == -1)
							curr_process.setStart_time(currentTime);

					}

				}

			}

			// Increasing CPU Time of Current Process
			if (curr_process != null) {
				curr_process.incCurrTimeCPU();
				curr_process.decRemainingTime();
			}

			// Increment Waiting Time For All Processes in All Ready Queue
			ready_queue.incrementWaitingTime();
			currentTime++;

			// Releasing Semaphores
			sem3.release();

			// Checking For Current Status of Thread Dispatching Processes to Ready Queue
			if (pushed != job_queue.size()) {
				sem1.release();
			} else {
				sem2.release();
			}
		}

		// Printing Gantt Chart
		System.out.println("\n~~~~Gantt Chart: \n");

		// Printing Process Information
		System.out.print("Process: ");
		for (int i = 0; i < gantt_chart.size(); i++) {

			System.out.printf("  |  %2d", gantt_chart.get(i).getId());
		}
		System.out.println("  |\n");
		System.out.print("Time:    ");

		// Printing Starting and Ending Duration of Each Process in Gantt Chart
		for (int i = 0; i < gantt_chart.size(); i++) {

			System.out.printf("%1s%3d%1s  ", "", gantt_chart.get(i).getStart(), "");
		}

		System.out.printf("%1s%3d%1s ", "", gantt_chart.get(gantt_chart.size() - 1).getEnd(), "");
		System.out.println("");

		// Printing Statistics
		calculateTurnaroundTime();
		calculateResponseTime();
		printTable();
		calculateTotalAvgTimes();
	}

	// Calculating Turnaround Time for Each Process
	public void calculateTurnaroundTime() {

		for (int i = 0; i < job_queue.size(); i++) {

			job_queue.get(i).setTurnaround_time(job_queue.get(i).getTermination_time() - 0);
		}
	}

	// Calculating Response Time for Each Process
	public void calculateResponseTime() {

		for (int i = 0; i < job_queue.size(); i++) {

			job_queue.get(i).setResponse_time(job_queue.get(i).getStart_time() - 0);
		}
	}

	// Function to Calculate Average Statistics
	void calculateTotalAvgTimes() {

		double total_turnaround_time = 0;
		double total_waiting_time = 0;
		double total_response_time = 0;

		// Calculating Average Turnaround, Waiting and Response Time
		for (int i = 0; i < job_queue.size(); i++) {

			total_turnaround_time += job_queue.get(i).getTurnaround_time();
			total_waiting_time += job_queue.get(i).getWaiting_time();
			total_response_time += job_queue.get(i).getResponse_time();
		}

		// Printing Scheduling Policy Statistics

		if (mode == 'f')
			System.out.print("\n~~~~FCFS Scheduling Statistics: \n\n");
		else if (mode == 'r')
			System.out.print("\n~~~~RR-10 Scheduling Statistics: \n\n");
		else if (mode == 's')
			System.out.print("\n~~~~SJF Scheduling Statistics: \n\n");

		System.out.printf("==> Average Turnaround Time = %g\n", (total_turnaround_time / job_queue.size()));
		System.out.printf("==> Average Waiting Time = %g\n", (total_waiting_time / job_queue.size()));
		System.out.printf("==> Average Response Time = %g\n\n\n", (total_response_time / job_queue.size()));

	}

	// Function to Print Process Statistics
	void printTable() {

		System.out.println("\n~~~~Process Statistics: \n");
		System.out.printf("%7s%10s%10s%10s%15s%18s%15s%17s\n\n", "PID", "Memory", "Burst", "Start", "Termination",
				"Turnaround Time", "Waiting Time", "Response Time");

		for (int i = 0; i < job_queue.size(); i++) {

			System.out.printf("%7s%10d%10d%10d%15d%18d%15d%17d\n", job_queue.get(i).getProcess_id(),
					job_queue.get(i).getMemory(), job_queue.get(i).getBurst(), job_queue.get(i).getStart_time(),
					job_queue.get(i).getTermination_time(), job_queue.get(i).getTurnaround_time(),
					job_queue.get(i).getWaiting_time(), job_queue.get(i).getResponse_time());

		}

	}

}

public class main {

	// Function to Execute Desired Scheduling Policy
	public static void executeScheduler(char mode, int quantum) {

		// Printing Respective Scheduling Message
		if (mode == 's')
			System.out
					.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~SHORTEST JOB FIRST~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n\n");
		else if (mode == 'r')
			System.out
					.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ROUND ROBIN (Q=10)~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n\n");
		else if (mode == 'f')
			System.out.println(
					"~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~FIRST COME FIRST SERVE~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n\n");

		// Creating Scheduler Object
		Scheduler scheduler = new Scheduler(mode, quantum);

		// ---Starting Independent Threads For Dispatching Processes to Job Queue and
		// Ready Queue
		scheduler.start();

		// Starting Scheduler Simulation
		scheduler.simulateScheduling();
		try {

			// Waiting For Independent Thread to Terminate
			scheduler.join();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	// ----Main Thread
	public static void main(String[] args) {

		// FCFS
		executeScheduler('f', 0);
		// SJF
		executeScheduler('s', 0);
		// RR-10
		executeScheduler('r', 10);
	}

}
