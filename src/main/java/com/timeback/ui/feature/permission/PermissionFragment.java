package com.timeback.ui.feature.permission;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.timeback.ui.domain.model.ScreenState;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * UI-01 권한 요청 화면.
 *
 * 처리하는 ScreenState:
 * - Initial: 초기 상태
 * - Loading: 권한 상태 확인 중
 * - Content(PermissionViewData): 권한 상태 표시
 * - Blocked(PERMISSION_REQUIRED): 권한 미허용 → 설정 안내
 * - Error: 권한 확인 실패
 */
@AndroidEntryPoint
public class PermissionFragment extends Fragment {

    private PermissionViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PermissionViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // TODO: return inflater.inflate(R.layout.fragment_permission, container, false);
        return null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        observeState();
    }

    private void observeState() {
        viewModel.state.observe(getViewLifecycleOwner(), state -> {
            if (state instanceof ScreenState.Loading) {
                // TODO: 로딩 표시
            } else if (state instanceof ScreenState.Content) {
                // TODO: PermissionViewData 표시 - 권한 상태에 따라 UI 분기
            } else if (state instanceof ScreenState.Blocked) {
                // TODO: 권한 설정 화면으로 안내하는 UI
            } else if (state instanceof ScreenState.Error) {
                // TODO: 에러 메시지 표시 + 재시도 버튼
            }
        });

        viewModel.actionResult.observe(getViewLifecycleOwner(), result -> {
            // TODO: 설정 열기 결과 처리
        });
    }
}
