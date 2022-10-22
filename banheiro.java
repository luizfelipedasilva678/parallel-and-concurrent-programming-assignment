import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

class BanheiroMonitor {
	public static int VAZIO = 0;
	public static int COM_MULHER = 1;	
	public static int COM_HOMEM = 2;
	public static int MAX_PESSOAS = 2;
	public int qtdPessoasNoBanheiro = 0;
	public int state;
	
	BanheiroMonitor() {
		this.state = BanheiroMonitor.VAZIO;
	}
	
	public synchronized void homem_quer_entrar(int homemId) {	
		if( this.state == BanheiroMonitor.COM_MULHER ) {
			this.go_to_sleep();
		}
		
		if( this.qtdPessoasNoBanheiro == BanheiroMonitor.MAX_PESSOAS) {
			this.go_to_sleep();
		}

		if( this.state == BanheiroMonitor.VAZIO ) {
			System.out.println("BANHEIRO COM HOMEM");
		}
		
		this.state = BanheiroMonitor.COM_HOMEM;
		this.qtdPessoasNoBanheiro += 1;
		System.out.println("HOMEM " + homemId + " entrou no banheiro");	
	}
	
	public synchronized void homem_sai(int homemId) {
		System.out.println("HOMEM " + homemId + " saiu do banheiro");
		
		this.qtdPessoasNoBanheiro -= 1;
		
		if ( this.qtdPessoasNoBanheiro == 0 ) {
			this.state = BanheiroMonitor.VAZIO;
			System.out.println("BANHEIRO VAZIO");
			notify();
		}
	}
	
	public synchronized void mulher_quer_entrar(int mulherId) {
		if( this.state == BanheiroMonitor.COM_HOMEM ) {
			this.go_to_sleep();
		}
		
		if( this.qtdPessoasNoBanheiro == BanheiroMonitor.MAX_PESSOAS) {
			this.go_to_sleep();
		}

		if( this.state == BanheiroMonitor.VAZIO ) {
			System.out.println("BANHEIRO COM MULHER");
		}
		
		this.state = BanheiroMonitor.COM_MULHER;
		this.qtdPessoasNoBanheiro += 1;
		System.out.println("MULHER " + mulherId + " entrou no banheiro");	
	}
		
	public synchronized void mulher_sai(int mulherId) {
		System.out.println("MULHER " + mulherId + " saiu do banheiro");
		
		this.qtdPessoasNoBanheiro -= 1;
		
		if ( this.qtdPessoasNoBanheiro == 0 ) {
			this.state = BanheiroMonitor.VAZIO;
			System.out.println("BANHEIRO VAZIO");
			notify();
		}
	}
	
	private void go_to_sleep(){
		try{
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
	private Homem[] homens;
	
	GeradorHomens(int quantidadeDeHomens, BanheiroMonitor banheiroMonitor) {	
		this.quantidadeDeHomens = quantidadeDeHomens;
		this.banheiroMonitor = banheiroMonitor;
		this.homens = new Homem[quantidadeDeHomens];
	}
	
	public void run() {
		Timer timer = new Timer();
		GeradorHomens $this = this;
		
		timer.schedule(new TimerTask() {
			public void run() {
				for(int i = 0; i < quantidadeDeHomens; i++) {
					$this.homens[i] = new Homem(i, banheiroMonitor);
					$this.homens[i].run();
				}
				timer.cancel();
				
			}
		}, RandomNumber.generate(1, 4));	
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
	private Mulher[] mulheres;
	
	GeradorMulheres(int quantidadeDeMulheres, BanheiroMonitor banheiroMonitor) {
		this.quantidadeDeMulheres = quantidadeDeMulheres;
		this.banheiroMonitor = banheiroMonitor;
		this.mulheres = new Mulher[quantidadeDeMulheres];
	}
	
	public void run() {
		Timer timer = new Timer();
		GeradorMulheres $this = this;
		
		timer.schedule(new TimerTask() {
			public void run() {
				for(int i = 0; i < quantidadeDeMulheres; i++) {
					$this.mulheres[i] = new Mulher(i, banheiroMonitor);
					$this.mulheres[i].run();
				}

				timer.cancel();
			}
		}, 1);	
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
		geradorDeHomens.run();
		geradorDeMulheres.run();
		
		try {
			geradorDeHomens.join();
			geradorDeMulheres.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
