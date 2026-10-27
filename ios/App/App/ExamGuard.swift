import UIKit
import Capacitor

/// Reports screenshots and screen recording / mirroring while an exam is active.
/// iOS cannot block a screenshot beforehand; it can only notify after the fact, so JS ends the exam.
@objc(ExamGuardPlugin)
public class ExamGuardPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "ExamGuardPlugin"
    public let jsName = "ExamGuard"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "enable", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "disable", returnType: CAPPluginReturnPromise)
    ]

    private var enabled = false
    private var observers: [NSObjectProtocol] = []

    private var isCaptured: Bool {
        UIApplication.shared.connectedScenes
            .compactMap { ($0 as? UIWindowScene)?.screen }
            .contains { $0.isCaptured }
    }

    @objc func enable(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            if !self.enabled {
                self.enabled = true
                let center = NotificationCenter.default
                self.observers.append(center.addObserver(forName: UIApplication.userDidTakeScreenshotNotification, object: nil, queue: .main) { [weak self] _ in
                    self?.notifyListeners("screenshot", data: [:])
                })
                self.observers.append(center.addObserver(forName: UIScreen.capturedDidChangeNotification, object: nil, queue: .main) { [weak self] _ in
                    if self?.isCaptured == true { self?.notifyListeners("recording", data: [:]) }
                })
            }
            if self.isCaptured { self.notifyListeners("recording", data: [:]) }
            call.resolve(["secure": false, "screenshotDetection": true, "recordingDetection": true])
        }
    }

    @objc func disable(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            self.observers.forEach { NotificationCenter.default.removeObserver($0) }
            self.observers.removeAll()
            self.enabled = false
            call.resolve()
        }
    }
}

/// Main view controller that registers the in-app plugin (Main.storyboard points to this class).
class MainViewController: CAPBridgeViewController {
    override func capacitorDidLoad() {
        bridge?.registerPluginInstance(ExamGuardPlugin())
    }
}
