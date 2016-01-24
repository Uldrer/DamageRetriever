package application;

import java.util.ArrayList;

import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import MuDamage.BattleInfo;
import MuDamage.DamageRetriever;
import MuDamage.MemberStorage;
import MuDamage.ProgressEvent;
import MuDamage.ProgressListener;

public class GUI {

	protected Shell shell;
	private Text battleId;
	private Text weightValue;
	private StringListViewer muListViewer;
	private StyledText resultText;
	private List battleIdList;
	private List muList;
	private List memberList;
	private List memberBattleDmgList;
	private List memberT2DmgList;
	private List memberEnemyDmgList;
	private Button btnGetDamage;
	private Combo btnChooseWeight;
	private boolean defendingChecked;
	private Label lblMuMembers;
	private Label lblLicense;
	private ProgressBar progressBar;
	private DamageRetriever dmgRetriever;
	private LicenseChecker licenseChecker;
	private ArrayList<BattleInfo> battles;
	private Thread retrieveThread;
	private boolean retrievingData;
	private final double PROGRAM_VERSION = 0.913;
	private final String PROGRAM_NAME = "MU - Battle Damage Retriever";
	private final int DMG_LIST_WIDTH = 100;
	private final int MEMBER_LIST_WIDTH = 120;
	private final int MEMBER_START_Y = 500;
	private final int SHELL_HEIGHT = 768;
	private final int SHELL_WIDTH = 1024;
	private final int TEXT_SEPARATION = 5;
	private final int TEXT_HEIGHT_SEPARATION = 10;
	private String currentlySelectedMu;
	private final String[] COMBO_WEIGHTS = { "Advantage round threshold",
			"Waste round threshold", "Overkill round threshold",
			"Tight round weight", "Advantage round weight",
			"Waste round weight", "Overkill round weight", "T2 weight",
			"T3 weight", "T5 weight", "T10 weight", "T30 weight", "T120 weight" };
	private final static String WEIGHT_EXPLANATION = "The weight computation works in the following way: \n \n dmg*TIME_WEIGHT*ROUND_WEIGHT \n Where TIME_WEIGHT is T2 if dmg was done the last 2 minutes \n T3 if it was done between 2 and 3 minutes from the end, etc. \n ROUND_WEIGHT is dependent of the outcome of the round. \n If Advantage threshold is 0.55 and Waste threshold 0.56 \n then advantage weight is used if the winner of the round has 55-56% of the total dmg, etc. \n \n";

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			GUI window = new GUI();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {

		// Check license
		// System.out.println("Initiating...");

//		licenseChecker = new LicenseChecker();
//		if (!licenseChecker.checkLicense()) {
//			shell = new Shell();
//			shell.setSize(SHELL_WIDTH, SHELL_HEIGHT);
//			shell.setText(PROGRAM_NAME);
//
//			lblLicense = new Label(shell, SWT.NONE);
//			lblLicense
//					.setText("License check failed, contact Uldrer for further information.");
//			lblLicense.setBounds(200, 200, 700, 424);
//			FontData[] fontData = lblLicense.getFont().getFontData();
//			for (int i = 0; i < fontData.length; ++i) {
//				fontData[i].setHeight(18);
//			}
//			final Font newFont = new Font(shell.getDisplay(), fontData);
//			lblLicense.setFont(newFont);
//
//			// Since you created the font, you must dispose it
//			lblLicense.addDisposeListener(new DisposeListener() {
//				public void widgetDisposed(DisposeEvent e) {
//					newFont.dispose();
//				}
//			});
//
//			return;
//		}

		dmgRetriever = new DamageRetriever();
		battles = new ArrayList<BattleInfo>();

		shell = new Shell();
		shell.setSize(SHELL_WIDTH, SHELL_HEIGHT);
		shell.setText(PROGRAM_NAME);

		TextViewer textViewer = new TextViewer(shell, SWT.BORDER | SWT.V_SCROLL
				| SWT.H_SCROLL);
		resultText = textViewer.getTextWidget();
		resultText.setEditable(false);
		resultText.setBounds(370, 220, 600, 424);
		resultText.setText(WEIGHT_EXPLANATION
				+ "Retrieving MU-Member information...");

		progressBar = new ProgressBar(shell, SWT.NONE);
		progressBar.setBounds(10, 422, 170, 17);

		Label lblMembers = new Label(shell, SWT.NONE);
		lblMembers.setText("Members");
		lblMembers.setBounds(MEMBER_START_Y, 52, 50, 15);

		lblMuMembers = new Label(shell, SWT.NONE);
		lblMuMembers.setText("");
		lblMuMembers.setBounds(MEMBER_START_Y + lblMembers.getBounds().width
				+ TEXT_SEPARATION, 52, 250, 15);

		ListViewer listviewer_2 = new ListViewer(shell, SWT.BORDER
				| SWT.V_SCROLL);
		memberList = listviewer_2.getList();
		memberList.setBounds(MEMBER_START_Y, 93 + 2 * TEXT_HEIGHT_SEPARATION,
				MEMBER_LIST_WIDTH, 68);
		memberList.getVerticalBar().addSelectionListener(
				new SelectionListener() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						int selection = memberList.getVerticalBar()
								.getSelection();
						memberBattleDmgList.setTopIndex(selection);
						memberT2DmgList.setTopIndex(selection);
						memberEnemyDmgList.setTopIndex(selection);
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						int selection = memberList.getVerticalBar()
								.getSelection();
						memberBattleDmgList.setTopIndex(selection);
						memberT2DmgList.setTopIndex(selection);
						memberEnemyDmgList.setTopIndex(selection);
					}

				});

		ListViewer listviewer_3 = new ListViewer(shell, SWT.BORDER);
		memberBattleDmgList = listviewer_3.getList();
		memberBattleDmgList.setBounds(MEMBER_START_Y + MEMBER_LIST_WIDTH,
				93 + 2 * TEXT_HEIGHT_SEPARATION, DMG_LIST_WIDTH, 68);

		ListViewer listviewer_4 = new ListViewer(shell, SWT.BORDER);
		memberT2DmgList = listviewer_4.getList();
		memberT2DmgList.setBounds(MEMBER_START_Y + DMG_LIST_WIDTH
				+ MEMBER_LIST_WIDTH, 93 + 2 * TEXT_HEIGHT_SEPARATION,
				DMG_LIST_WIDTH, 68);

		ListViewer listviewer_5 = new ListViewer(shell, SWT.BORDER);
		memberEnemyDmgList = listviewer_5.getList();
		memberEnemyDmgList.setBounds(MEMBER_START_Y + 2 * DMG_LIST_WIDTH
				+ MEMBER_LIST_WIDTH, 93 + 2 * TEXT_HEIGHT_SEPARATION,
				DMG_LIST_WIDTH, 68);

		Label lblMemberNames = new Label(shell, SWT.NONE);
		lblMemberNames.setText("Name");
		lblMemberNames.setBounds(MEMBER_START_Y, 72 + TEXT_HEIGHT_SEPARATION,
				MEMBER_LIST_WIDTH, 15);

		Label lblMemberBattleDmg = new Label(shell, SWT.NONE);
		lblMemberBattleDmg.setText("Battle Damage");
		lblMemberBattleDmg.setBounds(MEMBER_START_Y + MEMBER_LIST_WIDTH,
				72 + TEXT_HEIGHT_SEPARATION, DMG_LIST_WIDTH, 15);

		Label lblMemberT2Dmg = new Label(shell, SWT.NONE);
		lblMemberT2Dmg.setText("T2 Damage");
		lblMemberT2Dmg.setBounds(MEMBER_START_Y + DMG_LIST_WIDTH
				+ MEMBER_LIST_WIDTH, 72 + TEXT_HEIGHT_SEPARATION,
				DMG_LIST_WIDTH, 15);

		Label lblMemberEnemyDmg = new Label(shell, SWT.NONE);
		lblMemberEnemyDmg.setText("Damage for enemy");
		lblMemberEnemyDmg.setBounds(MEMBER_START_Y + 2 * DMG_LIST_WIDTH
				+ MEMBER_LIST_WIDTH, 72 + TEXT_HEIGHT_SEPARATION,
				DMG_LIST_WIDTH + 20, 15);

		btnGetDamage = new Button(shell, SWT.NONE);
		btnGetDamage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				if (!retrievingData) {
					retrievingData = true;
					progressBar.setSelection(0);
					dmgRetriever.SetBattles(battles);

					retrieveThread = new Thread(dmgRetriever);
					retrieveThread.start();
				} else {
					resultText.setText(WEIGHT_EXPLANATION
							+ "Please wait. Retrieving API data.");
				}
			}
		});
		btnGetDamage.setBounds(10, 457, 100, 25);
		btnGetDamage.setText("Get damage");
		btnGetDamage.setEnabled(false);

		muListViewer = new StringListViewer(shell, SWT.BORDER | SWT.V_SCROLL
				| SWT.H_SCROLL);
		muList = muListViewer.getList();
		muList.setBounds(263, 93 + 2 * TEXT_HEIGHT_SEPARATION, 187, 68);
		muList.setItems(dmgRetriever.getMilitaryUnitInfo());
		muListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						String mu = muListViewer.getStringSelection();
						currentlySelectedMu = mu;
						lblMuMembers.setText(mu);
						MemberStorage mbStore = dmgRetriever
								.getMilitaryUnitMemberInfo(mu);
						memberList.setItems(mbStore.getNames());
						memberBattleDmgList.setItems(mbStore.getBattelDmg());
						memberT2DmgList.setItems(mbStore.getT2Dmg());
						memberEnemyDmgList.setItems(mbStore.getEnemyDmg());
					}
				});

		final Color myColor = new Color(Display.getCurrent(), 255, 0, 0);
		memberEnemyDmgList.setForeground(myColor);
		memberEnemyDmgList.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				myColor.dispose();
			}
		});

		Label lblApplication = new Label(shell, SWT.NONE);
		lblApplication.setFont(SWTResourceManager.getFont("Segoe UI", 12,
				SWT.NORMAL));
		lblApplication.setBounds(SHELL_WIDTH / 2 - 100, 10, 330, 50);
		lblApplication.setText(PROGRAM_NAME);

		Label lblBattleIds = new Label(shell, SWT.NONE);
		lblBattleIds.setBounds(26, 72, 150, 15);
		lblBattleIds.setText("Battle ids selected");

		Label lblMus = new Label(shell, SWT.NONE);
		lblMus.setText("Military Units");
		lblMus.setBounds(263, 72, 100, 15);

		battleId = new Text(shell, SWT.BORDER);
		battleId.setBounds(26, 204 + 2 * TEXT_HEIGHT_SEPARATION, 92, 23);

		ListViewer listViewer = new ListViewer(shell, SWT.BORDER | SWT.V_SCROLL);
		battleIdList = listViewer.getList();
		battleIdList.setBounds(27, 93 + 2 * TEXT_HEIGHT_SEPARATION, 187, 68);

		defendingChecked = false;
		Button btnCheckDefender = new Button(shell, SWT.CHECK);
		btnCheckDefender.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				defendingChecked = !defendingChecked;
			}
		});
		btnCheckDefender
				.setBounds(26, 230 + 2 * TEXT_HEIGHT_SEPARATION, 91, 25);
		btnCheckDefender.setText("Defenders");

		Button btnAddBattleId = new Button(shell, SWT.NONE);
		btnAddBattleId.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				String bIdText = battleId.getText();
				int bId = 0;
				boolean defender = defendingChecked;

				if (bIdText.equals("")) {
					return;
				}

				try {
					bId = Integer.parseInt(bIdText);
				} catch (NumberFormatException exception) {
					battleId.setText("");
					return;
				}

				System.out.println("Battle id: " + bId + " added.");
				battleId.setText("");

				battles.add(new BattleInfo(bId, defender));
				String[] info = new String[battles.size()];
				int j = 0;
				for (BattleInfo battle : battles) {
					info[j] = battle.getId().toString() + "     ";
					if (battle.isDefender()) {
						info[j] += "defending side";
					} else {
						info[j] += "attacking side";
					}
					j++;
				}

				battleIdList.setItems(info);
			}
		});
		btnAddBattleId.setBounds(140, 202 + 2 * TEXT_HEIGHT_SEPARATION, 91, 25);
		btnAddBattleId.setText("Add");

		Label lblBattleId = new Label(shell, SWT.NONE);
		lblBattleId.setBounds(27, 183 + TEXT_HEIGHT_SEPARATION, 91, 15);
		lblBattleId.setText("Battle id");

		weightValue = new Text(shell, SWT.BORDER);
		weightValue.addListener(SWT.KeyUp, new Listener() {
			public void handleEvent(Event e) {
				if (!retrievingData) {
					int selectionIndex = btnChooseWeight.getSelectionIndex();
					String text = weightValue.getText();
					double val;
					try {
						val = Double.parseDouble(text);
						if (val >= 0 && val <= 1) {
							dmgRetriever.setWeight(selectionIndex, val);
						}
					} catch (NumberFormatException exception) {
						// Not correctly set value
					}
				}
			}
		});
		weightValue.setBounds(26, 264 + 2 * TEXT_HEIGHT_SEPARATION, 92, 23);

		btnChooseWeight = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
		btnChooseWeight.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				int selecteionIndex = btnChooseWeight.getSelectionIndex();

				String[] weights = dmgRetriever.getWeights();
				weightValue.setText(weights[selecteionIndex]);

			}
		});
		btnChooseWeight.setBounds(140, 264 + 2 * TEXT_HEIGHT_SEPARATION, 170,
				23);
		btnChooseWeight.setItems(COMBO_WEIGHTS);
		btnChooseWeight.select(0);
		weightValue.setText(dmgRetriever.getWeights()[0]);

		Label versionLabel = new Label(shell, SWT.COLOR_DARK_GRAY);
		versionLabel.setBounds(10, SHELL_HEIGHT - 80, 300, 15);
		versionLabel.setText("Created by Uldrer.                    Version: "
				+ PROGRAM_VERSION);

		Button btnRemoveBattleId = new Button(shell, SWT.NONE);
		btnRemoveBattleId.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				String bIdText = battleId.getText();
				int bId = 0;

				if (bIdText.equals("")) {
					String[] idsSelected = battleIdList.getSelection();
					if (idsSelected.length > 0) {
						bIdText = idsSelected[0];
					} else {
						return;
					}
				}

				try {
					String[] tokens = bIdText.split(" ");
					bId = Integer.parseInt(tokens[0]);
				} catch (NumberFormatException exception) {
					battleId.setText("");
					return;
				}

				int index = battles.indexOf(new BattleInfo(bId));
				if (index == -1) {
					battleId.setText("");
					return;
				}
				battles.remove(index);
				String[] info = new String[battles.size()];
				int j = 0;
				for (BattleInfo battle : battles) {
					info[j] = battle.getId().toString() + "     ";
					if (battle.isDefender()) {
						info[j] += "defending side";
					} else {
						info[j] += "attacking side";
					}
					j++;
				}

				battleIdList.setItems(info);
				System.out.println("Battle id: " + bId + " removed.");
				battleId.setText("");
			}
		});
		btnRemoveBattleId.setBounds(240, 202 + 2 * TEXT_HEIGHT_SEPARATION, 91,
				25);
		btnRemoveBattleId.setText("Remove");

		dmgRetriever.addProgressListener(new ProgressListener() {
			@Override
			public void progressEvent(ProgressEvent e) {

				double percent = e.getPercent();

				if (percent == 1) {

					MemberStorage mbStore;
					switch (e.getType()) {
					case BATTLE:
						// all done
						String result = dmgRetriever.getResult();

						doResultTextUpdate(resultText, result);
						retrievingData = false;
						mbStore = dmgRetriever
								.getMilitaryUnitMemberInfo(currentlySelectedMu);
						doListUpdate(memberList, mbStore.getNames());
						doListUpdate(memberBattleDmgList,
								mbStore.getBattelDmg());
						doListUpdate(memberT2DmgList, mbStore.getT2Dmg());
						doListUpdate(memberEnemyDmgList, mbStore.getEnemyDmg());
						doProgressBarUpdate(progressBar, percent);

						break;
					case MU:
						doResultTextUpdate(resultText,
								"Finished parsing mu members.");
						mbStore = dmgRetriever
								.getMilitaryUnitMemberInfo(currentlySelectedMu);
						doListUpdate(memberList, mbStore.getNames());
						doListUpdate(memberBattleDmgList,
								mbStore.getBattelDmg());
						doListUpdate(memberT2DmgList, mbStore.getT2Dmg());
						doListUpdate(memberEnemyDmgList, mbStore.getEnemyDmg());
						setDmgBtnEnabled(btnGetDamage, true);
						break;

					}
				} else {
					doProgressBarUpdate(progressBar, percent);
				}

			}
		});

	}

	private static void doProgressBarUpdate(final ProgressBar target,
			final double value) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (!target.isDisposed()) {
					target.setSelection((int) (100 * value));
					target.getParent().layout();
				}
			}
		});
	}

	private static void doResultTextUpdate(final StyledText target,
			final String value) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (!target.isDisposed()) {
					target.setText(WEIGHT_EXPLANATION + value);
					target.getParent().layout();
				}
			}
		});
	}

	private static void doListUpdate(final List target, final String[] value) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (!target.isDisposed()) {
					target.setItems(value);
					target.getParent().layout();
				}
			}
		});
	}

	private static void setDmgBtnEnabled(final Button target,
			final boolean value) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (!target.isDisposed()) {
					target.setEnabled(value);
					target.getParent().layout();
				}
			}
		});
	}

}
