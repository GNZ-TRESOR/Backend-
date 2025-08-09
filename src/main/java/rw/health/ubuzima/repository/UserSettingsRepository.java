package rw.health.ubuzima.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rw.health.ubuzima.entity.UserSettings;
import rw.health.ubuzima.entity.User;
import rw.health.ubuzima.enums.SettingCategory;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {

    List<UserSettings> findByUser(User user);

    List<UserSettings> findByUserAndSettingCategory(User user, SettingCategory category);

    Optional<UserSettings> findByUserAndSettingCategoryAndSettingKey(
            User user, SettingCategory category, String settingKey);

    @Query("SELECT us FROM UserSettings us WHERE us.user.id = :userId")
    List<UserSettings> findByUserId(@Param("userId") Long userId);

    @Query("SELECT us FROM UserSettings us WHERE us.user.id = :userId AND us.settingCategory = :category")
    List<UserSettings> findByUserIdAndCategory(@Param("userId") Long userId, @Param("category") SettingCategory category);

    void deleteByUserAndSettingCategory(User user, SettingCategory category);

    void deleteByUserAndSettingCategoryAndSettingKey(User user, SettingCategory category, String settingKey);
}
