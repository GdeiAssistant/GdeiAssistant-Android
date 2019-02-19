package edu.gdei.gdeiassistant.Activity;

import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.gdei.gdeiassistant.Constant.ActivityRequestCodeConstant;
import edu.gdei.gdeiassistant.Constant.ChangeAvatarOptionConstant;
import edu.gdei.gdeiassistant.Constant.MainItemTagConstant;
import edu.gdei.gdeiassistant.Constant.PermissionRequestCodeConstant;
import edu.gdei.gdeiassistant.Fragment.FragmentCet;
import edu.gdei.gdeiassistant.Fragment.FragmentGrade;
import edu.gdei.gdeiassistant.Fragment.FragmentIndex;
import edu.gdei.gdeiassistant.Fragment.FragmentSchedule;
import edu.gdei.gdeiassistant.Pojo.Entity.Access;
import edu.gdei.gdeiassistant.Presenter.MainPresenter;
import edu.gdei.gdeiassistant.R;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private AlertDialog alertDialog;

    private Toolbar mainToolbar;

    private DrawerLayout mainDrawerLayout;

    private NavigationView mainNavView;

    private CircleImageView mainNavAvatar;

    private TextView mainNavWelcomeText;

    private FragmentIndex fragmentIndex;

    private FragmentGrade fragmentGrade;

    private FragmentSchedule fragmentSchedule;

    private FragmentCet fragmentCet;

    private int currentItemID;

    private MainPresenter mainPresenter;

    public MainPresenter getMainPresenter() {
        return mainPresenter;
    }

    private long exitTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //配置加载Presenter
        mainPresenter = new MainPresenter(this);
        //初始化控件
        InitView();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(Bundle outState) {

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ChangeCurrentItem(currentItemID);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        //移除所有的回调和消息，防止内存泄露
        mainPresenter.RemoveCallBacksAndMessages();
        //注销广播
        mainPresenter.UnregisterReceiver();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case PermissionRequestCodeConstant.PERMISSION_ALBUM:
                    mainPresenter.GetPhotoFromAlbum();
                    break;

                case PermissionRequestCodeConstant.PERMISSION_CAMERA:
                    mainPresenter.GetPhotoFromCamera();
                    break;

                case PermissionRequestCodeConstant.LOAD_AVATAR:
                    mainPresenter.InitUserProfile();
                    break;
            }
        } else {
            this.ShowToast("用户拒绝了权限申请，应用的部分功能可能无法使用");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ActivityRequestCodeConstant.RESULT_PHOTO_FROM_ALBUM:
                //从相册得到图片，进行裁剪
                if (data != null) {
                    mainPresenter.StartPhotoZoom(data.getData());
                }
                break;

            case ActivityRequestCodeConstant.RESULT_PHOTO_FROM_CAMERA:
                //调用相机拍照得到图片，进行裁剪
                mainPresenter.StartCamera(getApplicationContext());
                break;

            case ActivityRequestCodeConstant.RESULT_SAVE_PHOTO_TO_VIEW:
                //取得裁剪后的图片，进行保存
                mainPresenter.SaveAvatarAndSetPhotoToView(data);
                break;

            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 初始化控件
     */
    private void InitView() {
        mainToolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(mainToolbar);

        mainDrawerLayout = findViewById(R.id.mainDrawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mainDrawerLayout, mainToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mainDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mainNavView = findViewById(R.id.mainNavView);
        mainNavView.setNavigationItemSelectedListener(this);
        mainNavView.setCheckedItem(R.id.index);

        mainNavAvatar = (mainNavView.getHeaderView(0).findViewById(R.id.mainNavAvatar));
        mainNavAvatar.setOnClickListener(this);
        mainPresenter.InitUserProfile();

        mainNavWelcomeText = (mainNavView.getHeaderView(0).findViewById(R.id.mainNavWelcomeText));
        //初始化Fragment
        SwitchFragment(MainItemTagConstant.TAG_INDEX);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mainNavAvatar:
                //修改头像
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle("修改头像选项").setItems(new String[]{"从相册选择图片", "拍照"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                switch (which) {
                                    case ChangeAvatarOptionConstant.GET_PHOTO_FROM_ALBUM:
                                        alertDialog.dismiss();
                                        mainPresenter.GetPhotoFromAlbum();
                                        break;

                                    case ChangeAvatarOptionConstant.GET_PHOTO_FROM_CAMERA:
                                        alertDialog.dismiss();
                                        mainPresenter.GetPhotoFromCamera();
                                        break;
                                }
                            }
                        });
                if (alertDialog != null) {
                    alertDialog.dismiss();
                }
                alertDialog = builder.create();
                alertDialog.show();
                break;
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.index) {
            CloseDrawer();
            SwitchFragment(MainItemTagConstant.TAG_INDEX);
            ChangeCurrentItem(MainItemTagConstant.TAG_INDEX);
        } else if (id == R.id.grade) {
            CloseDrawer();
            SwitchFragment(MainItemTagConstant.TAG_GRADE);
            ChangeCurrentItem(MainItemTagConstant.TAG_GRADE);
        } else if (id == R.id.schedule) {
            CloseDrawer();
            SwitchFragment(MainItemTagConstant.TAG_SCHEDULE);
            ChangeCurrentItem(MainItemTagConstant.TAG_SCHEDULE);
        } else if (id == R.id.cet) {
            CloseDrawer();
            SwitchFragment(MainItemTagConstant.TAG_CET);
            ChangeCurrentItem(MainItemTagConstant.TAG_CET);
        } else if (id == R.id.evaluate) {
            CloseDrawer();
            startActivity(new Intent(this, EvaluateActivity.class));
        } else if (id == R.id.card) {
            CloseDrawer();
            startActivity(new Intent(this, CardActivity.class));
        } else if (id == R.id.charge) {
            CloseDrawer();
            startActivity(new Intent(this, ChargeActivity.class));
        } else if (id == R.id.lost) {
            CloseDrawer();
            startActivity(new Intent(this, LostActivity.class));
        } else if (id == R.id.exit) {
            //弹出退出确认框
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("退出账号");
            builder.setMessage("你确认要退出当前账号吗？");
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mainPresenter.Logout();
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            if (alertDialog != null) {
                alertDialog.dismiss();
            }
            alertDialog = builder.create();
            alertDialog.show();
        } else if (id == R.id.about) {
            CloseDrawer();
            startActivity(new Intent(this, AboutSoftWareActivity.class));
        } else if (id == R.id.update) {
            mainPresenter.CheckUpgrade();
        }
        return true;
    }

    /**
     * 弹出补丁冷启动提示
     */
    public void ShowPatchRelaunchTip() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("新补丁更新");
        alertDialogBuilder.setMessage("新补丁已成功安装，请重启应用以生效");
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("关闭应用", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mainPresenter.PatchRelaunchAndStopProcess();
            }
        });
        alertDialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

            }
        });
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /**
     * 显示更新提示
     *
     * @param versionCodeName
     * @param versionInfo
     * @param downloadURL
     */
    public void ShowUpgradeTip(String versionCodeName, String versionInfo, final String downloadURL, String fileSize) {
        AlertDialog.Builder updateDialogBuilder = new AlertDialog.Builder(this);
        updateDialogBuilder.setTitle("新版本更新");
        StringBuilder dialogMessage = new StringBuilder("新版本:" + versionCodeName + "，大小:" + fileSize);
        String infos[] = versionInfo.split(";");
        for (String string : infos) {
            dialogMessage.append("\n");
            dialogMessage.append(string);
        }
        updateDialogBuilder.setMessage(dialogMessage);
        updateDialogBuilder.setPositiveButton("更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mainPresenter.DownLoadNewVersion(downloadURL);
            }
        });
        updateDialogBuilder.setNegativeButton("暂不更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        updateDialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

            }
        });
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        alertDialog = updateDialogBuilder.create();
        alertDialog.show();
    }

    /**
     * 关闭侧边栏
     */
    private void CloseDrawer() {
        DrawerLayout drawer = findViewById(R.id.mainDrawerLayout);
        drawer.closeDrawer(GravityCompat.START);
    }

    /**
     * 显示系统需要申请相机权限的提示
     *
     * @param requestCode
     */
    public void ShowRequestCameraPermissionDialog(final int requestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("申请拍照或录像的权限").setCancelable(false)
                .setMessage("易小助需要获取拍照或录像的权限，用于拍照和上传头像")
                .setPositiveButton("好", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //申请存储权限
                        mainPresenter.RequestCameraPermission(requestCode);
                        alertDialog.dismiss();
                    }
                }).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

            }
        });
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * 显示系统需要申请存储权限的提示
     *
     * @param requestCode
     */
    public void ShowRequestStoragePermissionDialog(final int requestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("申请读写手机存储的权限").setCancelable(false)
                .setMessage("易小助需要获取读写手机存储的权限，用于加载用户头像信息")
                .setPositiveButton("好", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //申请存储权限
                        mainPresenter.RequestStoragePermission(requestCode);
                        alertDialog.dismiss();
                    }
                }).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

            }
        });
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * 更新侧边栏欢迎提示文字
     *
     * @param name
     */
    public void UpdateMainNavigationWelcomeText(String name) {
        mainNavWelcomeText.setText("欢迎你，" + name);
    }

    /**
     * 更改Toolbar标题
     *
     * @param title
     */
    public void ChangeToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    /**
     * 切换Fragment页面
     *
     * @param position
     */
    public void SwitchFragment(int position) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        if (fragmentIndex != null) {
            fragmentTransaction.hide(fragmentIndex);
        }
        if (fragmentGrade != null) {
            fragmentTransaction.hide(fragmentGrade);
        }
        if (fragmentSchedule != null) {
            fragmentTransaction.hide(fragmentSchedule);
        }
        if (fragmentCet != null) {
            fragmentTransaction.hide(fragmentCet);
        }
        switch (position) {
            case MainItemTagConstant.TAG_INDEX:
                if (fragmentIndex == null) {
                    fragmentIndex = new FragmentIndex();
                    fragmentTransaction.add(R.id.mainFrameLayout, fragmentIndex);
                } else {
                    fragmentTransaction.show(fragmentIndex);
                }
                break;

            case MainItemTagConstant.TAG_GRADE:
                if (fragmentGrade == null) {
                    fragmentGrade = new FragmentGrade();
                    fragmentTransaction.add(R.id.mainFrameLayout, fragmentGrade);
                } else {
                    fragmentTransaction.show(fragmentGrade);
                }
                break;

            case MainItemTagConstant.TAG_SCHEDULE:
                if (fragmentSchedule == null) {
                    fragmentSchedule = new FragmentSchedule();
                    fragmentTransaction.add(R.id.mainFrameLayout, fragmentSchedule);
                } else {
                    fragmentTransaction.show(fragmentSchedule);
                }
                break;

            case MainItemTagConstant.TAG_CET:
                if (fragmentCet == null) {
                    fragmentCet = new FragmentCet();
                    fragmentTransaction.add(R.id.mainFrameLayout, fragmentCet);
                } else {
                    fragmentTransaction.show(fragmentCet);
                }
                break;
        }
        //提交Fragment事务
        fragmentTransaction.commitAllowingStateLoss();
    }

    /**
     * 修改当前选中的Item
     */
    public void ChangeCurrentItem(int position) {
        //修改当前ItemID
        currentItemID = position;
        //改变选中的Item和标题
        switch (currentItemID) {
            case MainItemTagConstant.TAG_INDEX:
                mainNavView.setCheckedItem(R.id.index);
                ChangeToolbarTitle("易小助");
                break;

            case MainItemTagConstant.TAG_GRADE:
                mainNavView.setCheckedItem(R.id.grade);
                ChangeToolbarTitle("成绩查询");
                break;

            case MainItemTagConstant.TAG_SCHEDULE:
                mainNavView.setCheckedItem(R.id.schedule);
                ChangeToolbarTitle("课表查询");
                break;

            case MainItemTagConstant.TAG_CET:
                mainNavView.setCheckedItem(R.id.cet);
                ChangeToolbarTitle("四六级查询");
                break;

            default:
                break;
        }
    }

    /**
     * 显示Toast消息
     *
     * @param text
     */
    public void ShowToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * 读取用户权限，显示对应功能菜单
     */
    public void LoadAccessAndShowMenu(Access access) {
        Menu menu = mainNavView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            if (menu.getItem(i).getItemId() == R.id.grade) {
                menu.getItem(i).setVisible(Boolean.TRUE.equals(access.getGrade()));
            }
            if (menu.getItem(i).getItemId() == R.id.schedule) {
                menu.getItem(i).setVisible(Boolean.TRUE.equals(access.getSchedule()));
                fragmentIndex.ShowScheduleModule();
                fragmentIndex.getIndexPresenter().TodayScheduleQuery();
            }
            if (menu.getItem(i).getItemId() == R.id.cet) {
                menu.getItem(i).setVisible(Boolean.TRUE.equals(access.getCet()));
            }
            if (menu.getItem(i).getItemId() == R.id.evaluate) {
                menu.getItem(i).setVisible(Boolean.TRUE.equals(access.getEvaluate()));
            }
            if (menu.getItem(i).getItemId() == R.id.card) {
                menu.getItem(i).setVisible(Boolean.TRUE.equals(access.getBill()));
                fragmentIndex.ShowCardModule();
                fragmentIndex.getIndexPresenter().CardInfoQuery();
            }
            if (menu.getItem(i).getItemId() == R.id.charge) {
                menu.getItem(i).setVisible(Boolean.TRUE.equals(access.getCharge()));
            }
            if (menu.getItem(i).getItemId() == R.id.lost) {
                menu.getItem(i).setVisible(Boolean.TRUE.equals(access.getLost()));
            }
        }
    }

    /**
     * 更新显示的头像
     *
     * @param drawable
     */
    public void SetAvatarImage(Drawable drawable) {
        mainNavAvatar.setImageDrawable(drawable);
    }

    /**
     * 当前Fragment不是首页，则返回主页
     * 当前Fragment是首页，则启动防误触退出机制
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            DrawerLayout drawer = findViewById(R.id.mainDrawerLayout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                //若当前Fragment是首页，则启动防误触退出机制
                if (currentItemID == 0) {
                    //防止用户意外返回键退出
                    if ((System.currentTimeMillis() - exitTime) > 2000) {
                        Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                        exitTime = System.currentTimeMillis();
                    } else {
                        finishAffinity();
                        System.exit(0);
                    }
                    return true;
                } else {
                    //若当前Fragment不是首页，则返回主页
                    SwitchFragment(MainItemTagConstant.TAG_INDEX);
                    ChangeCurrentItem(MainItemTagConstant.TAG_INDEX);
                }
            }
        }
        return true;
    }
}
