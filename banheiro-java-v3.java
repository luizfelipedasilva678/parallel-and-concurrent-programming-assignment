import java.util.Random;
import java.util.concurrent.*;
import java.util.Timer;
import java.util.TimerTask;

class BanheiroMonitor {
	public static int VAZIO = 0;
	public static int COM_MULHER = 1;	
	public static int COM_HOMEM = 2;
	public int qtdHomens = 0, qtdMulheres = 0;
	public int state;
	
	BanheiroMonitor() {
		this.state = BanheiroMonitor.VAZIO;
	}
	
	public synchronized void homem_quer_entrar(int homemId) {			
		while( this.state == BanheiroMonitor.COM_MULHER ) {
			this.go_to_sleep();
		}
	
		if( this.state == BanheiroMonitor.VAZIO ) {
			System.out.println("BANHEIRO COM HOMEM");
		}
		
		this.state = BanheiroMonitor.COM_HOMEM;
		this.qtdHomens++;
		System.out.println("HOMEM " + homemId + " entrou no banheiro");	
	}
	
	public synchronized void homem_sai(int homemId) {
		System.out.println("HOMEM " + homemId + " saiu do banheiro");
		this.qtdHomens--;
		
		if(this.qtdHomens == 0) {
			this.state = BanheiroMonitor.VAZIO;
			System.out.println("BANHEIRO VAZIO");			
		}
			
		notify();
		
	}
	
	public synchronized void mulher_quer_entrar(int mulherId) {
		while( this.state == BanheiroMonitor.COM_HOMEM ) {
			this.go_to_sleep();
		}
		
		if( this.state == BanheiroMonitor.VAZIO ) {
			System.out.println("BANHEIRO COM MULHER");
		}
		
		this.state = BanheiroMonitor.COM_MULHER;
		this.qtdMulheres++;
		System.out.println("MULHER " + mulherId + " entrou no banheiro");	
	}
		
	public synchronized void mulher_sai(int mulherId) {
		System.out.println("MULHER " + mulherId + " saiu do banheiro");
		this.qtdMulheres--;
		
		if(this.qtdMulheres == 0) {
			this.state = BanheiroMonitor.VAZIO;
			System.out.println("BANHEIRO VAZIO");			
		}
		
		notify();
	}
	
	private void go_to_sleep(){
		try {
			wait();
		} catch(InterruptedException e){
			System.out.println("Erro ao executar go_to_sleep " + e.getMessage());
		}
	}
}

class RandomNumber {
	public static int generate(int min, int max) {
		Random rand = new Random();
		
		return rand.nextInt((max - min) + 1) + min;
	}
}

class GeradorHomens extends Thread {
	private int quantidadeDeHomens;
	private BanheiroMonitor banheiroMonitor;
	private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	
	GeradorHomens(int quantidadeDeHomens, BanheiroMonitor banheiroMonitor) {	
		this.quantidadeDeHomens = quantidadeDeHomens;
		this.banheiroMonitor = banheiroMonitor;
	}
	
	public void run() {	
		for(int i = 0; i < this.quantidadeDeHomens; i++) {
			int delay = RandomNumber.generate(1, 4);
			scheduler.schedule(new Homem(i, banheiroMonitor), delay, TimeUnit.MILLISECONDS);
		}
		
		scheduler.shutdown();
	}
}

class Homem extends Thread {
	private int id;
	private BanheiroMonitor banheiroMonitor;
	private Timer timerHomem = new Timer();
	
	Homem(int id, BanheiroMonitor banheiroMonitor) {
		this.id = id;
		this.banheiroMonitor = banheiroMonitor;
	}
	
	public void run() {
		Homem $this = this;
		banheiroMonitor.homem_quer_entrar(this.id);
		timerHomem.schedule(new TimerTask() {
			public void run() {
				banheiroMonitor.homem_sai($this.id);
				$this.timerHomem.cancel();
			}
		}, 1);
	}
}

class GeradorMulheres extends Thread {
	private int quantidadeDeMulheres;
	private BanheiroMonitor banheiroMonitor;
	private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	
	GeradorMulheres(int quantidadeDeMulheres, BanheiroMonitor banheiroMonitor) {	
		this.quantidadeDeMulheres = quantidadeDeMulheres;
		this.banheiroMonitor = banheiroMonitor;
	}
	
	public void run() {
		for(int i = 0; i < this.quantidadeDeMulheres; i++) {
			int delay = 1;
			scheduler.schedule(new Mulher(i, banheiroMonitor), delay, TimeUnit.MILLISECONDS);
		}
		
		scheduler.shutdown();
	}
}

class Mulher extends Thread {
	private int id;
	private BanheiroMonitor banheiroMonitor;
	private Timer timerMulher = new Timer();
	
	Mulher(int id, BanheiroMonitor banheiroMonitor) {
		this.id = id;
		this.banheiroMonitor = banheiroMonitor;
	}
	
	public void run() {
		Mulher $this = this;
		banheiroMonitor.mulher_quer_entrar(this.id);
		timerMulher.schedule(new TimerTask() {
			public void run() {
				banheiroMonitor.mulher_sai($this.id);
				$this.timerMulher.cancel();
			}
		}, 2);
	}
}

public class banheiro {
	public static void main(String args[]) {
		BanheiroMonitor banheiro = new BanheiroMonitor();
		int quantidadeDeHomens = Integer.parseInt(args[0]);
		int quantidadeDeMulheres = Integer.parseInt(args[1]);
		GeradorHomens geradorDeHomens = new GeradorHomens(quantidadeDeHomens, banheiro);
		GeradorMulheres geradorDeMulheres = new GeradorMulheres(quantidadeDeMulheres, banheiro);
		geradorDeHomens.start();
		geradorDeMulheres.start();
		
		try {
			geradorDeHomens.join();
			geradorDeMulheres.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
