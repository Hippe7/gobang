import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Panel extends JPanel {//672是棋盘上能成五子的数目
	private int i, j, k, m, n, icount;
	private Color themeColor = Color.LIGHT_GRAY; // 默认主题颜色
	private int[][] board = new int[16][16];//0为玩家棋子，1是电脑棋子，2是空
	private boolean[][][] ptable = new boolean[16][16][672];
	private boolean[][][] ctable = new boolean[16][16][672];
	private int[][] cgrades = new int[16][16];
	private int[][] pgrades = new int[16][16];
	private int cgrade, pgrade;
	private int[][] win = new int[2][672];//连子数目，0是玩家，1是电脑
	private int oldx, oldy;
	private int bout = 1;
	private int pcount, ccount;//玩家、电脑各自的棋子数目
	private boolean player, computer, over, pwin, cwin, tie, start;//over结束，tie平局
	private int mat, nat, mde, nde;
	private boolean isPlayerFirst = true; // 新增：玩家是否先手
	private Timer computerTimer; // 新增：用于延迟白子落子
	private List<int[]> winningLine = new ArrayList<>(); // 新增：存储胜利的五颗棋子坐标

	public Panel() {
		addMouseListener(new Xiazi());
		this.ResetGame();
		
		// 初始化电脑延迟落子计时器
		computerTimer = new Timer(500, e -> {
			if (computer && !over) {
				ComTurn();
				repaint();
			}
		});
		computerTimer.setRepeats(false);
	}
	
	// 设置主题颜色
	public void setTheme(Color color) {
		this.themeColor = color;
		this.setBackground(color);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// 根据主题颜色设置棋盘线条和文字颜色
		if (themeColor.equals(Color.BLACK)) {
			g.setColor(Color.WHITE);
		} else {
			g.setColor(Color.BLACK);
		}
		
		for (int i = 0; i < 16; i++)
			for (int j = 0; j < 16; j++) {
				g.drawLine(50, 50 + j * 30, 500, 50 + j * 30);
			}
		for (int i = 0; i < 16; i++)
			for (int j = 0; j < 16; j++) {
				g.drawLine(50 + j * 30, 50, 50 + j * 30, 500);
			}
		for (int i = 0; i < 16; i++) {
			String number = Integer.toString(i);
			g.drawString(number, 46 + 30 * i, 45);
		}
		for (int i = 1; i < 16; i++) {
			String number = Integer.toString(i);
			g.drawString(number, 33, 53 + 30 * i);
		}
		updatePaint(g);
	}

	class Xiazi extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			if (!over) {
				oldx = e.getX();
				oldy = e.getY();
				mouseClick();
				repaint();
			}
		}
	}

	// 游戏初始化
	public void ResetGame() {
		// 初始化棋盘
		for (i = 0; i < 16; i++)
			for (j = 0; j < 16; j++) {
				this.pgrades[i][j] = 0;
				this.cgrades[i][j] = 0;
				this.board[i][j] = 2;
			}
		// 遍历所有的五连子可能情况的权值
		// 横
		for (i = 0; i < 16; i++)
			for (j = 0; j < 12; j++) {
				for (k = 0; k < 5; k++) {
					this.ptable[j + k][i][icount] = true;
					this.ctable[j + k][i][icount] = true;
				}
				icount++;
			}
		// 竖
		for (i = 0; i < 16; i++)
			for (j = 0; j < 12; j++) {
				for (k = 0; k < 5; k++) {
					this.ptable[i][j + k][icount] = true;
					this.ctable[i][j + k][icount] = true;
				}
				icount++;
			}
		// 右斜
		for (i = 0; i < 12; i++)
			for (j = 0; j < 12; j++) {
				for (k = 0; k < 5; k++) {
					this.ptable[j + k][i + k][icount] = true;
					this.ctable[j + k][i + k][icount] = true;
				}
				icount++;
			}
		// 左斜
		for (i = 0; i < 12; i++)
			for (j = 15; j >= 4; j--) {
				for (k = 0; k < 5; k++) {
					this.ptable[j - k][i + k][icount] = true;
					this.ctable[j - k][i + k][icount] = true;
				}
				icount++;
			}
		for (i = 0; i < 2; i++) // 初始化黑子白子上的每个权值上的连子数
			for (j = 0; j < 672; j++)
				this.win[i][j] = 0;
		this.player = isPlayerFirst; // 根据选择决定谁先手
		this.icount = 0;
		this.ccount = 0;
		this.pcount = 0;
		this.start = true;
		this.over = false;
		this.pwin = false;
		this.cwin = false;
		this.tie = false;
		this.bout = 1;
		winningLine.clear(); // 清空胜利棋子记录
	}
	
	// 新增：设置玩家是否先手
	public void setPlayerFirst(boolean isFirst) {
		this.isPlayerFirst = isFirst;
		ResetGame(); // 重新开始游戏
	}

	public void ComTurn() { // 找出电脑（白子）最佳落子点
		for (i = 0; i <= 15; i++)
			// 遍历棋盘上的所有坐标
			for (j = 0; j <= 15; j++) {
				this.pgrades[i][j] = 0; // 该坐标的黑子奖励积分清零
				if (this.board[i][j] == 2) // 在还没下棋子的地方遍历
					for (k = 0; k < 672; k++)
						// 遍历该棋盘可落子点上的黑子所有权值的连子情况，并给该落子点加上相应奖励分
						if (this.ptable[i][j][k]) {
							switch (this.win[0][k]) {
							case 1: // 一连子
								this.pgrades[i][j] += 5;
								break;
							case 2: // 两连子
								this.pgrades[i][j] += 50;
								break;
							case 3: // 三连子
								this.pgrades[i][j] += 180;
								break;
							case 4: // 四连子
								this.pgrades[i][j] += 400;
								break;
							}
						}
				this.cgrades[i][j] = 0;// 该坐标的白子的奖励积分清零
				if (this.board[i][j] == 2) // 在还没下棋子的地方遍历
					for (k = 0; k < 672; k++)
						// 遍历该棋盘可落子点上的白子所有权值的连子情况，并给该落子点加上相应奖励分
						if (this.ctable[i][j][k]) {
							switch (this.win[1][k]) {
							case 1: // 一连子
								this.cgrades[i][j] += 5;
								break;
							case 2: // 两连子
								this.cgrades[i][j] += 52;
								break;
							case 3: // 三连子
								this.cgrades[i][j] += 100;
								break;
							case 4: // 四连子
								this.cgrades[i][j] += 400;
								break;
							}
						}
			}
		if (this.start) { // 开始时白子落子坐标
			if (this.board[4][4] == 2) {
				m = 4;
				n = 4;
			} else {
				m = 5;
				n = 5;
			}
			this.start = false;
		} else {
			for (i = 0; i < 16; i++)
				for (j = 0; j < 16; j++)
					if (this.board[i][j] == 2) { // 找出棋盘上可落子点的黑子白子的各自最大权值，找出各自的最佳落子点
						if (this.cgrades[i][j] >= this.cgrade) {
							this.cgrade = this.cgrades[i][j];
							this.mat = i;
							this.nat = j;
						}
						if (this.pgrades[i][j] >= this.pgrade) {
							this.pgrade = this.pgrades[i][j];
							this.mde = i;
							this.nde = j;
						}
					}
			if (this.cgrade >= this.pgrade) { // 如果白子的最佳落子点的权值比黑子的最佳落子点权值大，则电脑的最佳落子点为白子的最佳落子点，否则相反
				m = mat;
				n = nat;
			} else {
				m = mde;
				n = nde;
			}
		}
		this.cgrade = 0;
		this.pgrade = 0;
		this.board[m][n] = 1; // 电脑下子位置
		ccount++;
		if ((ccount == 50) && (pcount == 50)) // 平局判断
		{
			this.tie = true;
			this.over = true;
		}
		for (i = 0; i < 672; i++) {
			if (this.ctable[m][n][i] && this.win[1][i] != 7)
				this.win[1][i]++; // 给白子的所有五连子可能的加载当前连子数
			if (this.ptable[m][n][i]) {
				this.ptable[m][n][i] = false;
				this.win[0][i] = 7;
			}
		}
		this.player = true; // 该人落子
		this.computer = false; // 电脑落子结束
	}

	public void mouseClick() {
		if (!this.over)
			if (this.player) {
				// 检查点击是否在棋盘范围内
				if (this.oldx < 520 && this.oldy < 520) {
					int m1 = m, n1 = n;
					m = (oldx - 33) / 30;
					n = (oldy - 33) / 30;
					
					// 检查落点是否在棋盘范围内
					if (m < 0 || m > 15 || n < 0 || n > 15) {
						javax.swing.JOptionPane.showMessageDialog(this, "Please click within the chessboard!", "Hint", javax.swing.JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					
					if (this.board[m][n] == 2) {
						this.bout++;
						this.board[m][n] = 0;
						pcount++;
						if ((ccount == 50) && (pcount == 50)) {
							this.tie = true;
							this.over = true;
						}
						for (i = 0; i < 672; i++) {
							if (this.ptable[m][n][i] && this.win[0][i] != 7)
								this.win[0][i]++; // 给黑子的所有五连子可能的加载当前连子数
							if (this.ctable[m][n][i]) {
								this.ctable[m][n][i] = false;
								this.win[1][i] = 7;
							}
						}
						this.player = false;
						this.computer = true;
					} else {
						m = m1;
						n = n1;
					}
				}
			}
	}

	public void updatePaint(Graphics g) {
		if (!this.over) { // 如果是轮到电脑下
			if (this.computer && !computerTimer.isRunning()) {
				// 延迟500毫秒后执行电脑落子
				computerTimer.start();
			} else if (!computerTimer.isRunning()) {
			// 遍历当前棋盘上的五连子情况，判断输赢
			for (i = 0; i <= 1; i++)
				for (j = 0; j < 672; j++) {
					if (this.win[i][j] == 5) {
						// 记录胜利的五颗棋子坐标
						winningLine.clear();
						for (int x = 0; x < 16; x++) {
							for (int y = 0; y < 16; y++) {
								if ((i == 0 && ptable[x][y][j]) || (i == 1 && ctable[x][y][j])) {
									winningLine.add(new int[]{x, y});
								}
							}
						}
						
						if (i == 0) { // 人赢
							this.pwin = true;
							this.over = true; // 游戏结束
							break;
						} else {
							this.cwin = true; // 电脑赢
							this.over = true;
							break;
						}
					}
					if (this.over) // 一遇到五连子退出棋盘遍历
				break;
			}
		}
			g.setFont(new Font("华文行楷", 0, 20));
			g.setColor(Color.RED);
			// 画出当前棋盘所有棋子
		for (i = 0; i <= 15; i++)
			for (j = 0; j <= 15; j++) {
				// 如果board元素值为0，则该坐标处为黑子
				if (this.board[i][j] == 0) {
					g.setColor(Color.BLACK);
					g.fillOval(i * 30 + 31, j * 30 + 31, 24, 24);
					// 在黑色主题下给黑子添加白色边框，使其更明显
					if (themeColor.equals(Color.BLACK)) {
						g.setColor(Color.WHITE);
						g.drawOval(i * 30 + 31, j * 30 + 31, 23, 23);
					}
				}
				// 如果board元素值为1，则该坐标处为白子
				if (this.board[i][j] == 1) {
					g.setColor(Color.WHITE);
					g.fillOval(i * 30 + 31, j * 30 + 31, 24, 24);
					// 给白子添加黑色边框，在白色主题下更明显
					g.setColor(Color.BLACK);
					g.drawOval(i * 30 + 31, j * 30 + 31, 23, 23);
				}
			}
		// 画出当前所下子的标记，便于辨认
		if (this.board[m][n] != 2) {
			g.setColor(Color.RED);
			g.drawRect(m * 30 + 30, n * 30 + 30, 26, 26);
		}
		
		// 胜利时圈出五颗棋子
		if (over && !tie) {
			g.setColor(Color.GREEN);
			g.setFont(new Font("黑体", Font.BOLD, 20));
			for (int[] pos : winningLine) {
				int x = pos[0];
				int y = pos[1];
				g.drawOval(x * 30 + 25, y * 30 + 25, 36, 36);
			}
		}
		
		// 判断输赢情况
		// 人赢
		if (this.pwin)
			g.drawString("恭喜你赢了！点击'开始游戏'重新开始。", 20, 200);
		// 电脑赢
		if (this.cwin)
			g.drawString("电脑赢了！点击'开始游戏'重新开始。", 20, 200);
		// 平局
		if (this.tie)
			g.drawString("平局！点击'开始游戏'重新开始。", 20, 200);
			g.dispose();
		}
	}
}
